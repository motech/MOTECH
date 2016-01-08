package org.motechproject.osgi.web.it.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.osgi.web.BundleContextWrapper;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.wait.ContextPublishedWaitCondition;
import org.motechproject.testing.osgi.wait.Wait;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class BundleContextWrapperBundleIT extends BasePaxIT {

    @Inject
    private BundleContext bundleContext;

    @Test
    public void testThatBundleContextWrapperReturnsCorrectApplicationContext() throws InterruptedException {
        BundleContextWrapper bundleContextWrapper = new BundleContextWrapper(bundleContext);

        Bundle bundle = bundleContext.getBundle();
        assertEquals(bundle.getSymbolicName(), bundleContextWrapper.getCurrentBundleSymbolicName());

        new Wait(new ContextPublishedWaitCondition(bundleContext), 5000).start();

        ApplicationContext applicationContextForCurrentBundle = bundleContextWrapper.getBundleApplicationContext();
        assertNotNull(applicationContextForCurrentBundle);

        Object testBundleContextWrapper = applicationContextForCurrentBundle.getBean("testBundleContextWrapper");
        assertNotNull(testBundleContextWrapper);
    }
}
