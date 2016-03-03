package org.motechproject.admin.messages;

import org.codehaus.jackson.annotate.JsonValue;

/**
 * Represents the level of a {@link org.motechproject.admin.domain.StatusMessage}, which is reflected on the UI.
 * Message levels are taken into consideration when processing notification rules.
 *
 * @see org.motechproject.admin.domain.StatusMessage
 * @see org.motechproject.admin.domain.NotificationRule
 */
public enum Level {
    CRITICAL("CRITICAL"), ERROR("ERROR"), WARN("WARN"), INFO("INFO"), DEBUG("DEBUG");

    private final String value;

    /**
     * @return the string representation of this level
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    private Level(String value) {
        this.value = value;
    }

    /**
     * Parses the string to create the enum instance. The parse is case-insensitive.
     * @param string The string to be parsed
     * @return The {@link Level} which this string represents or {@code null} if no match is found.
     */
    public static Level fromString(String string) {
        Level result = null;
        if (string != null) {
            for (Level level : Level.values()) {
                if (level.getValue().equals(string.toUpperCase())) {
                    result = level;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Checks if the level is higher or equal than the given value.
     *
     * @param level the level to check
     * @return true if given level is equal or less then this
     */
    public boolean containsLevel(Level level) {
        return this.ordinal() >= level.ordinal();
    }
}
