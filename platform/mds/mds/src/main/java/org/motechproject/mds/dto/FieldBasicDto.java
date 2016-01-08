package org.motechproject.mds.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The <code>FieldBasicDto</code> contains basic information about a field.
 */
public class FieldBasicDto {
    private String displayName;
    private String name;
    private boolean required;
    private boolean unique;
    private Object defaultValue;
    private String tooltip;
    private String placeholder;

    public FieldBasicDto() {
        this(null, null);
    }

    public FieldBasicDto(String displayName, String name) {
        this(displayName, name, false, false);
    }

    public FieldBasicDto(String displayName, String name, boolean required, boolean unique) {
        this(displayName, name, required, unique, "", "", "");
    }

    public FieldBasicDto(String displayName, String name, boolean required, boolean unique, Object defaultValue,
                         String tooltip, String placeholder) {
        this.displayName = displayName;
        this.name = name;
        this.required = required;
        this.unique = unique;
        this.defaultValue = defaultValue;
        this.tooltip = tooltip;
        this.placeholder = placeholder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
