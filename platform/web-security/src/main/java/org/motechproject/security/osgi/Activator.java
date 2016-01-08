package org.motechproject.security.osgi;

import org.apache.felix.http.api.ExtHttpService;
import org.motechproject.osgi.web.ModuleRegistrationData;
import org.motechproject.osgi.web.MotechOSGiWebApplicationContext;
import org.motechproject.osgi.web.UIFrameworkService;
import org.motechproject.osgi.web.exception.ServletRegistrationException;
import org.motechproject.osgi.web.ext.HttpContextFactory;
import org.motechproject.security.constants.PermissionNames;
import org.motechproject.security.filter.MotechDelegatingFilterProxy;
import org.motechproject.security.service.MotechProxyManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;

/**
 * The Spring security activator is used to register the spring security filter, dispatcher servlet,
 * and MotechProxyManager, which is necessary for supporting dynamic security. When initializing
 * the security chain, the DB will be consulted for security configuration, if it's not there then
 * the default security filter from the securityContext file is used.
 */
public class Activator implements BundleActivator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
    private static final String CONTEXT_CONFIG_LOCATION = "classpath:META-INF/osgi/applicationWebSecurityBundle.xml";
    private static final String SERVLET_URL_MAPPING = "/websecurity/api";
    private static final String RESOURCE_URL_MAPPING = "/websecurity";
    private static final String RESOURCE_URL_PATH = "websecurity";

    private ServiceTracker httpServiceTracker;
    private ServiceTracker uiServiceTracker;

    private static final String MODULE_NAME = "websecurity";

    private static BundleContext bundleContext;
    private static DelegatingFilterProxy filter;

    public static void setBundleContext(BundleContext context) {
        bundleContext = context;
    }

    /**
     * Called when this bundle is started so the Framework can
     * perform the bundle-specific activities necessary to start this bundle
     *
     * @param context of started bundle
     */
    @Override
    public void start(BundleContext context) {
        LOGGER.info("Starting web security bundle");
        setBundleContext(context);

        this.httpServiceTracker = new ServiceTracker(context,
                ExtHttpService.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference ref) {
                Object service = super.addingService(ref);
                serviceAdded((ExtHttpService) service);
                return service;
            }

            @Override
            public void removedService(ServiceReference ref, Object service) {
                serviceRemoved((ExtHttpService) service);
                super.removedService(ref, service);
            }
        };
        this.httpServiceTracker.open();

        this.uiServiceTracker = new ServiceTracker(context,
                UIFrameworkService.class.getName(), null) {

            @Override
            public Object addingService(ServiceReference ref) {
                Object service = super.addingService(ref);
                serviceAdded((UIFrameworkService) service);
                return service;
            }

            @Override
            public void removedService(ServiceReference ref, Object service) {
                serviceRemoved((UIFrameworkService) service);
                super.removedService(ref, service);
            }
        };
        this.uiServiceTracker.open();

        LOGGER.info("Started web security bundle");
    }

    /**
     * Called when this bundle is stopped so the Framework can
     * perform the bundle-specific activities necessary to stop the bundle.
     *
     * @param context of stopped bundle
     */
    public void stop(BundleContext context) {
        this.httpServiceTracker.close();
        this.uiServiceTracker.close();
    }

    public static class WebSecurityApplicationContext extends MotechOSGiWebApplicationContext {

        public WebSecurityApplicationContext() {
            super();
            setBundleContext(Activator.bundleContext);
        }

    }

    /**
     * Initializes the security chain by fetching the proxy manager,
     * registers the security filter and spring dispatcher servlet.
     */
    private void serviceAdded(ExtHttpService service) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setContextConfigLocation(CONTEXT_CONFIG_LOCATION);
        dispatcherServlet.setContextClass(WebSecurityApplicationContext.class);
        ClassLoader old = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            HttpContext httpContext = HttpContextFactory.getHttpContext(
                    service.createDefaultHttpContext(),
                    bundleContext.getBundle()
            );

            service.registerServlet(SERVLET_URL_MAPPING, dispatcherServlet, null, null);
            service.registerResources(RESOURCE_URL_MAPPING, "/webapp", httpContext);
            LOGGER.debug("Servlet registered");

            filter = new MotechDelegatingFilterProxy("springSecurityFilterChain", dispatcherServlet.getWebApplicationContext());
            MotechProxyManager proxyManager = dispatcherServlet.getWebApplicationContext().getBean(MotechProxyManager.class);

            LOGGER.debug("Creating initial proxy chain");
            proxyManager.initializeProxyChain();

            service.registerFilter(filter, "/.*", null, 0, httpContext);
            LOGGER.debug("Filter registered");
        } catch (NamespaceException e) {
            throw new ServletRegistrationException("Web-security servlet already registered", e);
        } catch (ServletException e) {
            throw new ServletRegistrationException("Unable to register servlet for web-security", e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Unregisters given service
     *
     * @param service that was removed
     */
    private void serviceRemoved(ExtHttpService service) {
        service.unregister(SERVLET_URL_MAPPING);
        LOGGER.debug("Servlet unregistered");

        service.unregisterFilter(filter);
        LOGGER.debug("Filter unregistered");
    }

    /**
     * Sets Web Security for added service
     *
     * @param service that was added
     */
    private void serviceAdded(UIFrameworkService service) {
        ModuleRegistrationData regData = new ModuleRegistrationData();
        regData.setModuleName(MODULE_NAME);
        regData.setUrl("../websecurity/index.html");
        regData.addAngularModule("webSecurity");
        regData.addSubMenu("/webSecurity/users", "security.manageUsers", PermissionNames.MANAGE_USER_PERMISSION);
        regData.addSubMenu("/webSecurity/roles", "security.manageRoles", PermissionNames.MANAGE_ROLE_AND_PERMISSION_PERMISSION);
        regData.addSubMenu("/webSecurity/permissions", "security.managePermissions", PermissionNames.MANAGE_ROLE_AND_PERMISSION_PERMISSION);
        regData.addSubMenu("/webSecurity/dynamicURL", "security.manageURL", PermissionNames.MANAGE_URL_PERMISSION);
        regData.addI18N("messages", "../websecurity/messages/");
        regData.setBundle(bundleContext.getBundle());
        regData.setResourcePath(RESOURCE_URL_PATH);

        service.registerModule(regData);
        LOGGER.debug("Web Security registered in UI framework");
    }

    /**
     * Unregisters given removed service
     *
     * @param service that was removed
     */
    private void serviceRemoved(UIFrameworkService service) {
        service.unregisterModule(MODULE_NAME);
        LOGGER.debug("Web Security unregistered from ui framework");
    }

}
