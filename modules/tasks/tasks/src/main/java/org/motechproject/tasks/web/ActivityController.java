package org.motechproject.tasks.web;

import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.util.Order;
import org.motechproject.tasks.domain.TaskActivity;
import org.motechproject.tasks.domain.TaskActivityType;
import org.motechproject.tasks.service.TaskActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Set;

/**
 * Controller for managing activities.
 */
@Controller
public class ActivityController {

    private TaskActivityService activityService;

    /**
     * Controller constructor.
     *
     * @param activityService  the activity service, not null
     */
    @Autowired
    public ActivityController(final TaskActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * Returns the list of recent activities.
     *
     * @return  the list of activities
     */
    @RequestMapping(value = "/activity", method = RequestMethod.GET)
    @ResponseBody
    public List<TaskActivity> getRecentActivities() {
        return activityService.getLatestActivities();
    }

    /**
     * Returns the list of activities for task with the given ID.
     *
     * @param taskId  the ID of the task
     * @return  the list of activities
     */
    @RequestMapping(value = "/activity/{taskId}", method = RequestMethod.GET)
    @ResponseBody
    public TaskActivityRecords getTaskActivities(@PathVariable Long taskId, GridSettings settings) {
        if (settings != null) {
            QueryParams params = new QueryParams(settings.getPage(), settings.getRows(), new Order("date", Order.Direction.DESC));
            Set<TaskActivityType> types = settings.getTypesFromString();

            List<TaskActivity> activities = activityService.getTaskActivities(taskId, types, params);
            long count = activityService.getTaskActivitiesCount(taskId, types);
            int totalPages = (int) Math.ceil((double) count / settings.getRows());

            return new TaskActivityRecords(settings.getPage(), totalPages, count, activities);
        } else {
            return null;
        }
    }

    /**
     * Returns the count of specified activity types for the task with the given ID.
     *
     * @param taskId  the ID of the task
     * @param activityType the type of activities to count; ERROR, WARNING or SUCCESS only
     * @return  the list of activities
     */
    @RequestMapping(value = "/activity/{taskId}/{activityType}", method = RequestMethod.GET)
    @ResponseBody
    public long getTaskActivityCount(@PathVariable Long taskId, @PathVariable String activityType) {
        TaskActivityType type = TaskActivityType.valueOf(activityType);
        return activityService.getTaskActivitiesCount(taskId, type);
    }

    /**
     * Deletes all activities for task with the given ID.
     *
     * @param taskId  the ID of the task
     */
    @RequestMapping(value = "/activity/{taskId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteActivitiesForTask(@PathVariable Long taskId) {
        activityService.deleteActivitiesForTask(taskId);
    }
}
