package org.motechproject.mds.listener.records;

import org.motechproject.mds.helper.MdsBundleHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * A base class for listeners in MDS. Since listeners get constructed by JDO,
 * we don't normally have access to the spring context of MDS entities. This class
 * retrieves this context by registering as a service listener that for the
 * {@link org.springframework.context.ApplicationContext}. After the context gets
 * retrieved it can be used for retrieving the service specified by the implementing listener.
 * If service retrieval is initiated before the context is available, a wait time of max 5 minutes
 * will begin.
 *
 * @param <T> the type of the service which is used by the implementing listener
 */
public abstract class BaseListener<T> implements ServiceListener {

    private static final int CTX_WAIT_TIME_MS = 5 * 60 * 1000; // 5 min

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Object ctxWaitLock = new Object();
    private final BundleContext bundleContext;

    private ApplicationContext applicationContext;
    private T service;

    public BaseListener() {
        // Listeners get constructed by JDO. Because of this, we must obtain required references
        // by hand.
        bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        try {
            bundleContext.addServiceListener(this, String.format("(%s=%s)", Constants.OBJECTCLASS,
                    ApplicationContext.class.getName()));
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(
                    "Invalid syntax. Should not happen, can indicate framework version issues", e);
        }
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (MdsBundleHelper.isMdsEntitiesBundle(event.getServiceReference().getBundle())) {
            if (event.getType() == ServiceEvent.REGISTERED) {
                synchronized (ctxWaitLock) {
                    applicationContext = (ApplicationContext) bundleContext.getService(event.getServiceReference());
                    ctxWaitLock.notify();
                }
            } else if (event.getType() == ServiceEvent.UNREGISTERING) {
                // new listeners will be created with the new context, this one should get unregistered
                bundleContext.removeServiceListener(this);
            }
        }
    }

    public ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            waitForCtx();
            if (applicationContext == null) {
                throw new IllegalStateException("Entities application context unavailable in: " + getClass());
            }
        }
        return applicationContext;
    }


    protected Logger getLogger() {
        return logger;
    }

    protected T getService() {
        if (service == null) {
            service = getApplicationContext().getBean(getServiceClass());
        }
        return service;
    }

    protected abstract Class<T> getServiceClass();

    private void waitForCtx() {
        synchronized (ctxWaitLock) {
            if (applicationContext == null) {
                try {
                    logger.debug("Waiting {} ms for the entities context", CTX_WAIT_TIME_MS);
                    ctxWaitLock.wait(CTX_WAIT_TIME_MS);
                } catch (InterruptedException e) {
                    logger.debug("Interrupted while waiting for the application context");
                }
            }
        }
    }
}
