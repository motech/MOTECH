package org.motechproject.scheduler.contract;

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.Period;
import org.motechproject.event.MotechEvent;

import java.util.Date;

/**
 * Job that will be fired every {@link org.joda.time.Period} of time
 */
public class RepeatingPeriodSchedulableJob extends SchedulableJob {
    private static final long serialVersionUID = 1L;

    private MotechEvent motechEvent;
    private Date startTime;
    private Date endTime;
    private Period repeatPeriod;
    private boolean ignorePastFiresAtStart;
    private boolean useOriginalFireTimeAfterMisfire;

    /**
     * Constructor. It will create a job, which will never end, won't ignore past fires at start and will use original fire time after misfire.
     * Start time, {@code MotechEvent} and repeat period are not assigned, which means that further usage, without setting them, can cause exceptions.
     */
    public RepeatingPeriodSchedulableJob() {
        endTime = null;
        ignorePastFiresAtStart = false;
        useOriginalFireTimeAfterMisfire = true;
    }

    /**
     * Constructor.
     *
     * @param motechEvent  the {@code MotechEvent} which will be fired when the job triggers, not null
     * @param startTime  the {@code Date} at which job should become ACTIVE, not null
     * @param endTime  the {@code Date} at which job should be stopped, null treated as never end
     * @param repeatPeriod the {@code Period} between job fires, not null
     * @param ignorePastFiresAtStart the flag defining whether job should ignore past fires at start or not
     */
    public RepeatingPeriodSchedulableJob(final MotechEvent motechEvent, final Date startTime, final Date endTime, final Period repeatPeriod, boolean ignorePastFiresAtStart) {
        this.motechEvent = motechEvent;
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeatPeriod = repeatPeriod;
        this.ignorePastFiresAtStart = ignorePastFiresAtStart;
        this.useOriginalFireTimeAfterMisfire = true;
    }

    public MotechEvent getMotechEvent() {
        return motechEvent;
    }

    public RepeatingPeriodSchedulableJob setMotechEvent(final MotechEvent motechEvent) {
        this.motechEvent = motechEvent;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public RepeatingPeriodSchedulableJob setStartTime(final Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getEndTime() {
        return endTime;
    }

    public RepeatingPeriodSchedulableJob setEndTime(final Date endTime) {
        this.endTime = endTime;
        return this;
    }

    public Period getRepeatPeriod() {
        return repeatPeriod;
    }

    public void setRepeatPeriod(Period repeatPeriod) {
        this.repeatPeriod = repeatPeriod;
    }

    public boolean isIgnorePastFiresAtStart() {
        return ignorePastFiresAtStart;
    }

    public RepeatingPeriodSchedulableJob setIgnorePastFiresAtStart(boolean ignorePastFiresAtStart) {
        this.ignorePastFiresAtStart = ignorePastFiresAtStart;
        return this;
    }

    public boolean isUseOriginalFireTimeAfterMisfire() {
        return useOriginalFireTimeAfterMisfire;
    }

    public RepeatingPeriodSchedulableJob setUseOriginalFireTimeAfterMisfire(boolean useOriginalFireTimeAfterMisfire) {
        this.useOriginalFireTimeAfterMisfire = useOriginalFireTimeAfterMisfire;
        return this;
    }

    @Override
    public String toString() {
        return "RepeatingSchedulableJob [motechEvent=" + motechEvent
                + ", startTime=" + startTime + ", endTime=" + endTime
                + ", repeatPeriod=" + repeatPeriod + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RepeatingPeriodSchedulableJob)) {
            return false;
        }
        RepeatingPeriodSchedulableJob job = (RepeatingPeriodSchedulableJob) obj;
        if (!ObjectUtils.equals(motechEvent, job.motechEvent)) {
            return false;
        } else if (!ObjectUtils.equals(startTime, job.startTime)) {
            return false;
        } else if (!ObjectUtils.equals(endTime, job.endTime)) {
            return false;
        } else if (!ObjectUtils.equals(repeatPeriod, job.repeatPeriod)) {
            return false;
        } else if (ignorePastFiresAtStart != job.ignorePastFiresAtStart) {
            return false;
        } else if (useOriginalFireTimeAfterMisfire != job.useOriginalFireTimeAfterMisfire) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + ObjectUtils.hashCode(motechEvent);
        hash = hash * 31 + ObjectUtils.hashCode(startTime);
        hash = hash * 31 + ObjectUtils.hashCode(endTime);
        hash = hash * 31 + ObjectUtils.hashCode(repeatPeriod);
        hash = hash * 31 + ObjectUtils.hashCode(ignorePastFiresAtStart);
        hash = hash * 31 + ObjectUtils.hashCode(useOriginalFireTimeAfterMisfire);

        return hash;
    }
}
