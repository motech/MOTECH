package org.motechproject.commons.api;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class responsible for casting classes.
 */
public final class ClassUtils {

    /**
     * Filters the given {@code Enumeration} searching for instances of the given class.
     *
     * @param clazz  the class used for filtering
     * @param enumeration  the filtered elements
     * @param <T>  the class used for filtering and returning properly cast objects
     * @return the list of instances of given class found in the enumeration
     */
    public static <T> List<T> filterByClass(Class<T> clazz, Enumeration enumeration) {
        List<T> list = new ArrayList<>();

        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object obj = enumeration.nextElement();

                if (clazz.isInstance(obj)) {
                    list.add(clazz.cast(obj));
                }
            }
        }

        return list;
    }

    /**
     * This is a utility class and should not be instantiated
     */
    private ClassUtils() {
    }
}
