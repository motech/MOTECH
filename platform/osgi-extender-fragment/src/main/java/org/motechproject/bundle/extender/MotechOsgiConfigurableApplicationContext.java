package org.motechproject.bundle.extender;

import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * This is the context class, which will be used by the extender for creating application contexts.
 */
public class MotechOsgiConfigurableApplicationContext extends OsgiBundleXmlApplicationContext implements ConfigurableWebApplicationContext {

    private ServletContext servletContext;
    private ServletConfig servletConfig;
    private String namespace;

    private final Object lock = new Object();
    private boolean initialized;

    /**
     * Constructs the new context using the provided configuration locations.
     * @param configurationLocations the configuration location (Spring xml configuration files)
     */
    public MotechOsgiConfigurableApplicationContext(String[] configurationLocations) {
        super(configurationLocations);
        addApplicationListener(new ApplicationListener<ApplicationEvent>() {
            @Override
            public void onApplicationEvent(ApplicationEvent event) {
                if (event instanceof ContextRefreshedEvent) {
                    synchronized (lock) {
                        initialized = true;
                        lock.notifyAll();
                    }
                }
            }
        });
    }

    /**
     * Utility for waiting until the context is ready, which is signalled by firing the
     * {@link org.springframework.context.event.ContextRefreshedEvent}.
     * @param waitTimeInMillis the max wait in milliseconds
     */
    public void waitForContext(int waitTimeInMillis) {
        synchronized (lock) {
            if (!initialized) {
                try {
                    lock.wait(waitTimeInMillis);
                } catch (InterruptedException e) {
                    // stop waiting, context initialized
                }
            }
            // quiet period for context trackers
            try {
                lock.wait(waitTimeInMillis);
            } catch (InterruptedException e) {
                // done waiting
            }
        }
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void setConfigLocation(String configLocation) {
        this.setConfigLocations(new String[]{configLocation});
    }
}
