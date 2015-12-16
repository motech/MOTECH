package org.motechproject.tasks.service.impl;

import org.apache.commons.lang.StringUtils;
import org.motechproject.tasks.domain.DynamicChannelProvider;
import org.motechproject.tasks.domain.TaskTriggerInformation;
import org.motechproject.tasks.domain.TriggerEvent;
import org.motechproject.tasks.service.DynamicChannelLoader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DynamicChannelLoaderImpl implements DynamicChannelLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicChannelLoaderImpl.class);

    @Autowired
    private BundleContext bundleContext;

    public List<TriggerEvent> getDynamicTriggers(String channelModule, int page, int pageSize) {
        LOGGER.debug("Retrieving dynamic triggers for channel {}", channelModule);

        DynamicChannelProvider provider = getChannelProvider(channelModule);

        if (provider != null) {
            return provider.getTriggers(page, pageSize);
        }

        return new ArrayList<>();
    }

    public boolean hasDynamicTriggers(String moduleName) {
        LOGGER.debug("Retrieving dynamic triggers for channel {}", moduleName);

        DynamicChannelProvider provider = getChannelProvider(moduleName);

        return provider != null;
    }

    public Long countByChannelModuleName(String moduleName) {
        DynamicChannelProvider provider = getChannelProvider(moduleName);

        if (provider != null) {
            return provider.countTriggers();
        }

        return null;
    }

    public TriggerEvent getTrigger(TaskTriggerInformation triggerInformation) {
        DynamicChannelProvider provider = getChannelProvider(triggerInformation.getModuleName());

        if (provider != null) {
            return provider.getTrigger(triggerInformation.getTriggerListenerSubject());
        }

        return null;
    }

    private DynamicChannelProvider getChannelProvider(String channelModule) {
        DynamicChannelProvider provider = null;

        try {
            ServiceReference[] allRefs = bundleContext.getServiceReferences(DynamicChannelProvider.class.getName(), null);

            ServiceReference correctRef = findRefBySymbolicName(allRefs, channelModule);

            if (correctRef != null) {
                Object service = bundleContext.getService(correctRef);
                if (service instanceof DynamicChannelProvider) {
                    provider = (DynamicChannelProvider) service;
                    LOGGER.debug("Retrieved channel provider {} for channel {}", provider.getClass().getName(),
                            channelModule);
                } else {
                    LOGGER.warn("Channel provider {} for channel {} is invalid, it is not an instance of {}",
                            service.getClass().getName(), channelModule, DynamicChannelProvider.class.getName());
                }
            } else {
                LOGGER.debug("No channel provider available for channel {}", channelModule);
            }
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Error while retrieving provider references for channel {}", channelModule, e);
        }

        return provider;
    }

    private ServiceReference findRefBySymbolicName(ServiceReference[] refs, String symbolicName) {
        if (refs != null) {
            for (ServiceReference ref : refs) {
                if (StringUtils.equals(symbolicName, ref.getBundle().getSymbolicName())) {
                    return ref;
                }
            }
        }
        return null;
    }

    public TriggerEvent getTriggerBySubject(String subject) {
        try {
            ServiceReference[] allRefs = bundleContext.getServiceReferences(DynamicChannelProvider.class.getName(), null);

            for (ServiceReference ref : allRefs) {
                Object service = bundleContext.getService(ref);
                if (service instanceof DynamicChannelProvider) {
                    DynamicChannelProvider provider = (DynamicChannelProvider) service;
                    TriggerEvent triggerEvent = provider.getTrigger(subject);
                    if (triggerEvent != null) {
                        return triggerEvent;
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Error while retrieving provider references", e);
        }

        return null;
    }

    public boolean channelExists(String moduleName) {
        return getChannelProvider(moduleName) != null;
    }

    public boolean isValidTrigger(String moduleName, String subject) {
        DynamicChannelProvider provider = getChannelProvider(moduleName);
        return provider.validateSubject(subject);
    }
}
