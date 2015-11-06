package org.motechproject.mds.display;

import org.apache.bsf.util.MethodUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.domain.ManyToManyRelationship;
import org.motechproject.mds.domain.OneToManyRelationship;
import org.motechproject.mds.dto.FieldDto;
import org.motechproject.mds.util.Constants;
import org.motechproject.mds.util.PropertyUtil;
import org.motechproject.mds.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DisplayHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisplayHelper.class);

    private static final String ELLIPSIS = "...";

    public static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm Z");

    public static Object getDisplayValueForField(FieldDto field, Object value) {
        return getDisplayValueForField(field, value, null);
    }

    public static Object getDisplayValueForField(FieldDto field, Object value, Integer maxLength) {
        Object displayValue = null;

        if (value == null) {
            displayValue = null;
        } else if (field.getType().isRelationship()) {
            if (field.getType().isForClass(OneToManyRelationship.class) ||
                    field.getType().isForClass(ManyToManyRelationship.class)) {
                displayValue = buildDisplayValuesMapForRelationship((Collection) value, maxLength);
            } else {
                displayValue = buildDisplayValueForRelationship(value, maxLength);
            }
        } else if (field.getType().isCombobox()) {
            displayValue = getDisplayValueForCombobox(field, value);
        }

        return displayValue;
    }

    private static Object getDisplayValueForCombobox(FieldDto field, Object value) {
        Object displayValue;
        if (Constants.Util.FALSE.equalsIgnoreCase(field.getSettingsValueAsString(Constants.Settings.ALLOW_USER_SUPPLIED))) {
            String mapString = field.getSettingsValueAsString(Constants.Settings.COMBOBOX_VALUES);

            Map<String, String> comboboxValues = TypeHelper.parseStringToMap(String.class, String.class, mapString);

            if (value instanceof Collection) {
                Collection valuesToDisplay = new ArrayList();
                Collection enumList = (Collection) value;
                for (Object enumValue : enumList) {
                    String valueFromMap = comboboxValues.get(ObjectUtils.toString(enumValue));
                    valuesToDisplay.add(StringUtils.isNotEmpty(valueFromMap) ? valueFromMap : enumValue);
                }
                displayValue = valuesToDisplay;
            } else {
                String valueFromMap = comboboxValues.get(ObjectUtils.toString(value));
                displayValue = StringUtils.isNotEmpty(valueFromMap) ? valueFromMap : value;
            }
        } else {
            displayValue = value;
        }

        return displayValue;
    }

    private static Map<Long, String> buildDisplayValuesMapForRelationship(Collection values, Integer maxLength) {
        Map<Long, String> displayValues = new LinkedHashMap<>();
        for (Object obj : values) {
            Long key = (obj instanceof Long) ?
                    (Long) obj :
                    (Long) PropertyUtil.safeGetProperty(obj, Constants.Util.ID_FIELD_NAME);

            String value = buildDisplayValueForRelationship(obj, maxLength);

            displayValues.put(key, value);
        }
        return displayValues;
    }

    private static String buildDisplayValueForRelationship(Object value, Integer maxLength) {
        if (value instanceof Long) {
            return "#" + value;
        } else if (hasCustomToString(value)) {
            String toStringResult = value.toString();
            return applyMaxLength(toStringResult, maxLength);
        } else {
            Long id = (Long) PropertyUtil.safeGetProperty(value, Constants.Util.ID_FIELD_NAME);
            return id == null ? "" : id.toString();
        }
    }

    private static String applyMaxLength(String value, Integer maxLength) {
        return maxLength != null && value.length() > maxLength ?
                value.substring(0, maxLength + 1) + ELLIPSIS : value;
    }

    private static boolean hasCustomToString(Object value) {
        try {
            Method toStringMethod = MethodUtils.getMethod(value, "toString", new Class[0]);
            return !StringUtils.equals(Object.class.getName(), toStringMethod.getDeclaringClass().getName());
        } catch (NoSuchMethodException e) {
            LOGGER.error("Unable to retrieve toString() method for {}", value, e);
            return false;
        }
    }

    private DisplayHelper() {
    }
}
