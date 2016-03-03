package org.motechproject.mds.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The <code>TrackingDto</code> contains properties that describe the audit settings of an Entity,
 * such as whether to record history or publish CRUD events for a given Entity.
 */
public class TrackingDto {
    private boolean recordHistory;
    private boolean allowCreateEvent;
    private boolean allowUpdateEvent;
    private boolean allowDeleteEvent;
    private boolean modifiedByUser;
    private boolean nonEditable;

    public TrackingDto() {
        this(false, true, true, true, false, false);
    }

    public TrackingDto(boolean recordHistory, boolean allowCreateEvent, boolean allowUpdateEvent, boolean allowDeleteEvent, boolean modifiedByUser, boolean nonEditable) {
        this.recordHistory = recordHistory;
        this.allowCreateEvent = allowCreateEvent;
        this.allowDeleteEvent = allowDeleteEvent;
        this.allowUpdateEvent = allowUpdateEvent;
        this.modifiedByUser = modifiedByUser;
        this.nonEditable = nonEditable;
    }

    public boolean isRecordHistory() {
        return recordHistory;
    }

    public void setRecordHistory(boolean recordHistory) {
        this.recordHistory = recordHistory;
    }

    public boolean isAllowCreateEvent() {
        return allowCreateEvent;
    }

    public void setAllowCreateEvent(boolean allowCreateEvent) {
        this.allowCreateEvent = allowCreateEvent;
    }

    public boolean isAllowUpdateEvent() {
        return allowUpdateEvent;
    }

    public void setAllowUpdateEvent(boolean allowUpdateEvent) {
        this.allowUpdateEvent = allowUpdateEvent;
    }

    public boolean isAllowDeleteEvent() {
        return allowDeleteEvent;
    }

    public void setAllowDeleteEvent(boolean allowDeleteEvent) {
        this.allowDeleteEvent = allowDeleteEvent;
    }

    public void setAllEvents(boolean value) {
        this.allowCreateEvent = value;
        this.allowDeleteEvent = value;
        this.allowUpdateEvent = value;
    }

    public boolean isModifiedByUser() {
        return modifiedByUser;
    }

    public void setModifiedByUser(boolean modifiedByUser) {
        this.modifiedByUser = modifiedByUser;
    }

    public boolean isNonEditable() {
        return nonEditable;
    }

    public void setNonEditable(boolean nonEditable) {
        this.nonEditable = nonEditable;
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
