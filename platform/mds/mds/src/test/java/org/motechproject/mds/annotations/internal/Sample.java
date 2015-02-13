package org.motechproject.mds.annotations.internal;

import org.motechproject.commons.date.model.Time;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.CrudEvents;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.annotations.InSet;
import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.NotInSet;
import org.motechproject.mds.annotations.RestOperations;
import org.motechproject.mds.annotations.UIDisplayable;
import org.motechproject.mds.annotations.UIFilterable;
import org.motechproject.mds.domain.InstanceLifecycleListenerType;
import org.motechproject.mds.domain.RestOperation;
import org.motechproject.mds.event.CrudEventType;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity(recordHistory = true)
@RestOperations(RestOperation.DELETE)
@CrudEvents(CrudEventType.CREATE)
public class Sample {
    // if you added a new field (and it has no @Ignore annotation) please increase this number.
    public static final long FIELD_COUNT = 15;
    public static final long UI_DISPLAYABLE_FIELD_COUNT = 1;
    public static final long UI_FILTERABLE_FIELD_COUNT = 3;

    // test class

    @Field
    @UIFilterable
    private Boolean world;

    @UIFilterable
    @DecimalMin(value = "3")
    @DecimalMax(value = "4")
    @InSet(value = {"3", "3.14", "4"})
    @NotInSet(value = {"1", "2", "5"})
    public Integer pi;

    @DecimalMax(value = "1")
    @DecimalMin(value = "0")
    @InSet(value = {"1", "0.75", "0.5", "0.25", "0"})
    @NotInSet(value = {"-1", "2", "3"})
    public Double epsilon;

    @Min(value = 0)
    @Max(value = 10)
    public Integer random;

    @Max(value = 1)
    @Min(value = 0)
    public Double gaussian;

    @Pattern(regexp = "[A-Z][a-z]{9}")
    @Size(min = 10, max = 20)
    public String poem;

    @DecimalMin(value = "100")
    @DecimalMax(value = "500")
    public String article;

    @Ignore
    public String ignored;

    @UIDisplayable
    public Double money;

    @Column(length = 400)
    private String length400;

    private String ignoredPrivate;

    private Date serverDate;

    private Time localTime;

    @Field
    @Cascade(delete = true, update = false)
    private Set<RelatedSample> oneToManyUni;

    @Field
    @Persistent(mappedBy = "manyToOneBi")
    private List<RelatedSample> oneToManyBi;

    @Field
    private RelatedSample oneToOneUni;

    @Field
    @Persistent(mappedBy = "oneToOneBi2")
    private RelatedSample oneToOneBi;

    @Lookup
    public void lookupTest() {

    }

    public Boolean getWorld() {
        return world;
    }

    public void setWorld(Boolean world) {
        this.world = world;
    }

    @Field(displayName = "Server Date")
    @UIFilterable
    public Date getServerDate() {
        return serverDate;
    }

    public void setServerDate(Date serverDate) {
        this.serverDate = serverDate;
    }

    public Time getLocalTime() {
        return localTime;
    }

    @Field(required = true)
    public void setLocalTime(Time localTime) {
        this.localTime = localTime;
    }

    @Ignore
    public void setIgnoredPrivate(String ignoredPrivate) {
        this.ignoredPrivate = ignoredPrivate;
    }

    @Ignore
    public String getIgnoredPrivate() {
        return ignoredPrivate;
    }

    public String getLength400() {
        return length400;
    }

    public void setLength400(String length400) {
        this.length400 = length400;
    }

    public Set<RelatedSample> getOneToManyUni() {
        return oneToManyUni;
    }

    public void setOneToManyUni(Set<RelatedSample> oneToManyUni) {
        this.oneToManyUni = oneToManyUni;
    }

    @Cascade(persist = false, update = false, delete = true)
    public List<RelatedSample> getOneToManyBi() {
        return oneToManyBi;
    }

    public void setOneToManyBi(List<RelatedSample> oneToManyBi) {
        this.oneToManyBi = oneToManyBi;
    }

    public RelatedSample getOneToOneUni() {
        return oneToOneUni;
    }

    public void setOneToOneUni(RelatedSample oneToOneUni) {
        this.oneToOneUni = oneToOneUni;
    }

    public RelatedSample getOneToOneBi() {
        return oneToOneBi;
    }

    public void setOneToOneBi(RelatedSample oneToOneBi) {
        this.oneToOneBi = oneToOneBi;
    }

    @Ignore
    public static int getIgnoredStaticProperty() {
        return 13;
    }

    @InstanceLifecycleListener({InstanceLifecycleListenerType.POST_CREATE, InstanceLifecycleListenerType.POST_DELETE})
    public void correctListener(Sample sample) {
    }

    @InstanceLifecycleListener({})
    public void emptyValues(Sample sample) {
    }

    @InstanceLifecycleListener(InstanceLifecycleListenerType.POST_CREATE)
    public void badNumberOfParameters(Sample sample, String string) {
    }

    @InstanceLifecycleListener(InstanceLifecycleListenerType.POST_DELETE)
    public void badParameterType(String string) {
    }

    @InstanceLifecycleListener(InstanceLifecycleListenerType.POST_DELETE)
    public void correctPostDeleteListener(Sample sample) {
    }
}
