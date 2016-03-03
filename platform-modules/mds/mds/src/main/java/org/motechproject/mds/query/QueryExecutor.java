package org.motechproject.mds.query;

import org.apache.commons.lang.ArrayUtils;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.filter.Filters;
import org.motechproject.mds.util.InstanceSecurityRestriction;

import javax.jdo.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.motechproject.mds.util.SecurityUtil.getUsername;

/**
 * The <code>QueryExecutor</code> util class provides methods that help execute a JDO query.
 */
public final class QueryExecutor {

    private QueryExecutor() {
    }

    public static Object execute(Query query, InstanceSecurityRestriction restriction) {
        if (restriction != null && !restriction.isEmpty()) {
            return query.execute(getUsername());
        } else {
            return query.execute();
        }
    }

    public static Object execute(Query query, Object value, InstanceSecurityRestriction restriction) {
        return executeWithArray(query, new Object[] {value}, restriction, QueryType.RETRIEVE);
    }

    public static long executeDelete(Query query, Object value, InstanceSecurityRestriction restriction) {
        return executeDelete(query, new Object[] {value}, restriction);
    }

    public static long executeDelete(Query query, Object[] values, InstanceSecurityRestriction restriction) {
        return (long) executeWithArray(query, values, restriction, QueryType.DELETE);
    }

    public static Object executeWithArray(Query query, Object[] values,
                                          InstanceSecurityRestriction restriction) {
        return executeWithArray(query, values, restriction, QueryType.RETRIEVE);
    }

    private static Object executeWithArray(Query query, Object[] values,
                                          InstanceSecurityRestriction restriction, QueryType type) {
        // We unwrap ranges into two objects
        Object[] unwrappedValues = unwrap(values);
        Object[] callArray;

        if (restriction != null && !restriction.isEmpty() && unwrappedValues.length > 0) {
            callArray = ArrayUtils.add(unwrappedValues, getUsername());
        } else if (restriction != null && !restriction.isEmpty()) {
            callArray = new Object[] { getUsername() };
        } else {
            callArray = unwrappedValues;
        }

        if (type == QueryType.RETRIEVE) {
            return query.executeWithArray(callArray);
        } else {
            return query.deletePersistentAll(callArray);
        }
    }

    public static Object executeWithArray(Query query, List<Property> properties) {
        // We unwrap ranges into two objects
        Object[] unwrappedValues = unwrap(properties.toArray());

        return query.executeWithArray(unwrappedValues);
    }

    public static Object executeWithFilters(Query query, Filters filters,
                                            InstanceSecurityRestriction restriction) {
        return executeWithArray(query, filters.valuesForQuery(), restriction, QueryType.RETRIEVE);
    }

    private static Object[] unwrap(Object[] values) {
        List<Object> unwrapped = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(values)) {
            for (Object object : values) {
                if (object instanceof Range) {
                    Range range = (Range) object;
                    unwrapRange(unwrapped, range);
                } else if (object instanceof Set) {
                    Set set = (Set) object;
                    unwrapSet(unwrapped, set);
                } else if (object instanceof Property) {
                    Property property = (Property) object;
                    unwrapProperty(unwrapped, property);
                } else {
                    unwrapped.add(object);
                }
            }
        }

        return unwrapped.toArray();
    }

    private static void unwrapSet(Collection unwrappedCol, Set set) {
        for (Object o : set) {
            if (o != null) {
                unwrappedCol.add(o);
            }
        }
    }

    private static void unwrapRange(Collection unwrappedCol, Range range) {
        if (range != null && range.getMin() != null || range.getMax() != null) {
            unwrappedCol.add(range.getMin());
            unwrappedCol.add(range.getMax());
        }
    }

    private static void unwrapProperty(Collection unwrappedCol, Property property) {
        if (!property.shouldIgnoreThisProperty()) {
            Collection unwrap = property.unwrap();
            unwrappedCol.addAll(unwrap);
        }
    }

    private static enum  QueryType {
        RETRIEVE, DELETE
    }
}
