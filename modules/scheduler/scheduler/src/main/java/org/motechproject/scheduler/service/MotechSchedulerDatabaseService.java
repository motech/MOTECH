package org.motechproject.scheduler.service;

import org.motechproject.scheduler.constants.SchedulerConstants;
import org.motechproject.scheduler.contract.JobBasicInfo;
import org.motechproject.scheduler.contract.JobDetailedInfo;
import org.motechproject.scheduler.exception.MotechSchedulerJobRetrievalException;
import org.motechproject.scheduler.contract.JobsSearchSettings;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 *  Service provides methods used to get data from
 *  Scheduler. Also provides pagination to use with
 *  jqGrid.
 */
@PreAuthorize(SchedulerConstants.VIEW_SCHEDULER_JOBS)
public interface MotechSchedulerDatabaseService {

    /**
     * Returns info about scheduled jobs for given filter information
     * Sorts all jobs with ascending or descending order for given column.
     *
     * @param jobsSearchSettings contains filter, sorting and pagination jobs options.
     * @return list with {@link org.motechproject.scheduler.contract.JobBasicInfo}
     * for given sorting and pagination option
     * @throws MotechSchedulerJobRetrievalException when the query fails.
     */
    List<JobBasicInfo> getScheduledJobsBasicInfo(JobsSearchSettings jobsSearchSettings) throws MotechSchedulerJobRetrievalException;

    /**
     * Returns detailed information about job matching given {@code JobBasicInfo}.
     *
     * @param jobBasicInfo  the {@code JobBasicInfo} about the job
     * @return the detailed information about job
     * @throws MotechSchedulerJobRetrievalException when the query fails.
     */
    JobDetailedInfo getScheduledJobDetailedInfo(JobBasicInfo jobBasicInfo) throws MotechSchedulerJobRetrievalException;

    /**
     * Counts all triggers in TRIGGER table which matches the filters built from grid settings.
     *
     * @param jobsSearchSettings contains filter jobs information.
     * @return number of all triggers which matches the filters built from grid settings.
     * @throws MotechSchedulerJobRetrievalException when the query fails.
     */
    int countJobs(JobsSearchSettings jobsSearchSettings) throws MotechSchedulerJobRetrievalException;
}
