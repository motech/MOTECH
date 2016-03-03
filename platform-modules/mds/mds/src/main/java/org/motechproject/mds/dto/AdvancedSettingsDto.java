package org.motechproject.mds.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>AdvancedSettingsDto</code> contains information about advanced settings of an entity.
 */
public class AdvancedSettingsDto {
    private Long id;
    private Long entityId;
    private TrackingDto tracking = new TrackingDto();
    private List<LookupDto> indexes = new ArrayList<>();
    private RestOptionsDto restOptions = new RestOptionsDto();
    private BrowsingSettingsDto browsing = new BrowsingSettingsDto();
    private UserPreferencesDto userPreferences;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public TrackingDto getTracking() {
        return tracking;
    }

    public void setTracking(TrackingDto tracking) {
        this.tracking = null != tracking ? tracking : new TrackingDto();
    }

    public void addNewIndex(String lookupName) {
        this.indexes.add(new LookupDto(lookupName, true, false, true));
    }

    public void removeIndex(Integer idx) {
        if (null != idx && idx < indexes.size()) {
            indexes.remove(idx.intValue());
        }
    }

    public List<LookupDto> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<LookupDto> indexes) {
        this.indexes = null != indexes ? indexes : new ArrayList<LookupDto>();
    }

    public RestOptionsDto getRestOptions() {
        return restOptions;
    }

    public void setRestOptions(RestOptionsDto restOptions) {
        this.restOptions = restOptions;
    }

    public BrowsingSettingsDto getBrowsing() {
        return browsing;
    }

    public void setBrowsing(BrowsingSettingsDto browsing) {
        this.browsing = browsing;
    }

    public UserPreferencesDto getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(UserPreferencesDto userPreferences) {
        this.userPreferences = userPreferences;
    }

    @JsonIgnore
    public boolean isFieldExposedByRest(String fieldName) {
        return restOptions != null && restOptions.getFieldNames() != null &&
                restOptions.getFieldNames().contains(fieldName);
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
