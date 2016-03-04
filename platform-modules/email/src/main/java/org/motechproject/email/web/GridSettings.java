package org.motechproject.email.web;

import org.motechproject.commons.api.MotechEnumUtils;
import org.motechproject.email.domain.DeliveryStatus;

import java.util.Set;

/**
 * The <code>GridSettings</code> class provides an information about
 * available filtering and sorting options for control layer
 */

public class GridSettings {

    private String subject;
    private String deliveryStatus;
    private String timeFrom;
    private String timeTo;
    // sort column
    private String sidx;
    // sort direction
    private String sord;
    private Integer rows;
    private Integer page;

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getSord() {
        return sord;
    }

    public void setSord(String sord) {
        this.sord = sord;
    }

    public String getSidx() {
        return sidx;
    }

    public void setSidx(String sidx) {
        this.sidx = sidx;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String status) {
        this.deliveryStatus = status;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public Set<DeliveryStatus> getDeliveryStatusFromSettings() {
        return MotechEnumUtils.toEnumSet(DeliveryStatus.class, deliveryStatus);
    }
}
