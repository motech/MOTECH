package org.motechproject.mds.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>InstanceLifecycleListener</code> annotation is used to point methods
 * from the services exposed by OSGi that should listen to persistence events. The InstanceLifecycleListenerType
 * value is an array of one or more values specified in {@link InstanceLifecycleListenerType}
 * enum, that is: POST_CREATE, PRE_DELETE, POST_DELETE, POST_LOAD, PRE_STORE, POST_STORE.
 * The annotated methods must have only one parameter. If no package is specified, the parameter type
 * is a persistable class. Otherwise it has to be of type Object.
 * <p/>
 * This annotation is processed by
 * {@link org.motechproject.mds.annotations.internal.InstanceLifecycleListenerProcessor}.
 *
 * @see org.motechproject.mds.annotations.internal.InstanceLifecycleListenerProcessor
 * @see InstanceLifecycleListenerType
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InstanceLifecycleListener {

    /**
     * An array of instance lifecycle transitions, that will trigger method execution.
     */
    InstanceLifecycleListenerType[] value();

    /**
     * If specified, the listener will be registered for all persistable classes
     * within the provided package.
     */
    String packageName() default "";
}
