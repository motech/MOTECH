package org.motechproject.tasks.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.motechproject.commons.api.DataProvider;
import org.motechproject.commons.api.TasksEventParser;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventListener;
import org.motechproject.event.listener.EventListenerRegistryService;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListenerEventProxy;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.tasks.domain.Task;
import org.motechproject.tasks.domain.TaskActionInformation;
import org.motechproject.tasks.domain.TriggerEvent;
import org.motechproject.tasks.ex.TaskHandlerException;
import org.motechproject.tasks.ex.TriggerNotFoundException;
import org.motechproject.tasks.util.SchedulerTaskTriggerUtil;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.motechproject.tasks.events.constants.EventDataKeys.HANDLER_ERROR_PARAM;
import static org.motechproject.tasks.events.constants.EventDataKeys.TASK_FAIL_FAILURE_DATE;
import static org.motechproject.tasks.events.constants.EventDataKeys.TASK_FAIL_FAILURE_NUMBER;
import static org.motechproject.tasks.events.constants.EventDataKeys.TASK_FAIL_MESSAGE;
import static org.motechproject.tasks.events.constants.EventDataKeys.TASK_FAIL_STACK_TRACE;
import static org.motechproject.tasks.events.constants.EventDataKeys.TASK_FAIL_TASK_ID;
import static org.motechproject.tasks.events.constants.EventDataKeys.TASK_FAIL_TASK_NAME;
import static org.motechproject.tasks.events.constants.EventDataKeys.TASK_FAIL_TRIGGER_DISABLED;
import static org.motechproject.tasks.events.constants.EventSubjects.createHandlerFailureSubject;
import static org.motechproject.tasks.events.constants.EventSubjects.createHandlerSuccessSubject;
import static org.motechproject.tasks.events.constants.TaskFailureCause.TRIGGER;
import static org.motechproject.tasks.service.HandlerPredicates.withServiceName;

/**
 * The <code>TaskTriggerHandler</code> receives events and executes tasks for which the trigger
 * event subject is the same as the received event subject.
 */
@Service
public class TaskTriggerHandler implements TriggerHandler {

    private static final String TASK_POSSIBLE_ERRORS_KEY = "task.possible.errors";
    private static final String SCHEDULER_TASK_TRIGGER_WILDCARD = "org.motechproject.tasks.scheduler.";

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTriggerHandler.class);

    private TaskService taskService;
    private TaskActivityService activityService;
    private EventListenerRegistryService registryService;
    private EventRelay eventRelay;
    private SettingsFacade settings;
    private Map<String, DataProvider> dataProviders;
    private MotechSchedulerService schedulerService;
    private SchedulerTaskTriggerUtil schedulerTaskTriggerUtil;

    private TaskActionExecutor executor;

    @Autowired
    public TaskTriggerHandler(TaskService taskService, TaskActivityService activityService,
                              EventListenerRegistryService registryService, EventRelay eventRelay,
                              TaskActionExecutor taskActionExecutor,
                              @Qualifier("tasksSettings") SettingsFacade settings,
                              MotechSchedulerService schedulerService,
                              SchedulerTaskTriggerUtil schedulerTaskTriggerUtil) {
        this.taskService = taskService;
        this.activityService = activityService;
        this.registryService = registryService;
        this.eventRelay = eventRelay;
        this.settings = settings;
        this.executor = taskActionExecutor;
        this.schedulerService = schedulerService;
        this.schedulerTaskTriggerUtil = schedulerTaskTriggerUtil;
    }

    @PostConstruct
    public void registerHandlerForSavedTasks(){
        for (Task task : taskService.getAllTasks()) {
            registerHandlerFor(task.getTrigger().getEffectiveListenerSubject());
        }
    }

    @Override
    public final void registerHandlerFor(String subject) {
        LOGGER.info("Registering handler for {}", subject);

        String serviceName = "taskTriggerHandler";
        Method method = ReflectionUtils.findMethod(this.getClass(), "handle", MotechEvent.class);
        Object obj = CollectionUtils.find(
                registryService.getListeners(subject), withServiceName(serviceName)
        );

        try {
            if (method != null && obj == null) {
                EventListener proxy = new MotechListenerEventProxy(serviceName, this, method);

                registryService.registerListener(proxy, subject);
                LOGGER.info(String.format("%s listens on subject %s", serviceName, subject));
            }
        } catch (RuntimeException exp) {
            LOGGER.error(
                    String.format("%s can not listen on subject %s due to:", serviceName, subject),
                    exp
            );
        }

        if (subject.startsWith(SCHEDULER_TASK_TRIGGER_WILDCARD)) {
            schedulerTaskTriggerUtil.scheduleTriggerJob(subject);
        }
    }

    @Override
    public void handle(MotechEvent event) throws TriggerNotFoundException {
        LOGGER.info("Handling the motech event with subject: {}", event.getSubject());

        // Look for custom event parser
        Map<String, Object> eventParams = event.getParameters();

        TasksEventParser parser = null;
        if (eventParams != null) {
            parser = taskService.findCustomParser((String) eventParams.get(TasksEventParser.CUSTOM_PARSER_EVENT_KEY));
        }

        List<Task> tasks;
        if (event.getSubject().startsWith(SCHEDULER_TASK_TRIGGER_WILDCARD)){
            tasks = Arrays.asList(schedulerTaskTriggerUtil.getSingleTaskBySubject(event.getSubject()));
        } else {
            // Use custom event parser, if it exists, to modify event
            TriggerEvent trigger = taskService.findTrigger(parser == null ? event.getSubject() : parser.parseEventSubject(event.getSubject(), event.getParameters()));
            tasks = taskService.findActiveTasksForTrigger(trigger);
        }

        Map<String, Object> parameters = parser == null ? event.getParameters() : parser.parseEventParameters(event.getSubject(), event.getParameters());


        for (Task task : tasks) {
            TaskContext taskContext = new TaskContext(task, parameters, activityService);
            TaskInitializer initializer = new TaskInitializer(taskContext);

            try {
                LOGGER.info("Executing all actions from task: {}", task.getName());
                if (initializer.evalConfigSteps(dataProviders)) {
                    for (TaskActionInformation action : task.getActions()) {
                        executor.execute(task, action, taskContext);
                    }
                    handleSuccess(parameters, task);
                }
                LOGGER.warn("Actions from task: {} weren't executed, because config steps didn't pass the evaluation", task.getName());
            } catch (TaskHandlerException e) {
                handleError(parameters, task, e);
            } catch (RuntimeException e) {
                handleError(parameters, task, new TaskHandlerException(TRIGGER, "task.error.unrecognizedError", e));
            }
        }
    }

    private void handleError(Map<String, Object> params, Task task, TaskHandlerException e) {
        LOGGER.warn("Omitted task: {} with ID: {} because: {}", task.getName(), task.getId(), e);

        activityService.addError(task, e);
        task.incrementFailuresInRow();

        LOGGER.warn("The number of failures for task: {} is: {}", task.getName(), task.getFailuresInRow());

        int failureNumber = task.getFailuresInRow();
        int possibleErrorsNumber = getPossibleErrorsNumber();

        if (failureNumber >= possibleErrorsNumber) {
            task.setEnabled(false);

            activityService.addWarning(task);
            publishTaskDisabledMessage(task.getName());
        }

        taskService.save(task, !task.getTrigger().getEffectiveListenerSubject().startsWith(SCHEDULER_TASK_TRIGGER_WILDCARD));

        Map<String, Object> errorParam = new HashMap<>();
        errorParam.put(TASK_FAIL_MESSAGE, e.getMessage());
        errorParam.put(TASK_FAIL_STACK_TRACE, ExceptionUtils.getStackTrace(e));
        errorParam.put(TASK_FAIL_FAILURE_DATE, DateTime.now());
        errorParam.put(TASK_FAIL_FAILURE_NUMBER, failureNumber);
        errorParam.put(TASK_FAIL_TRIGGER_DISABLED, task.isEnabled());
        errorParam.put(TASK_FAIL_TASK_ID, task.getId());
        errorParam.put(TASK_FAIL_TASK_NAME, task.getName());

        params.put(HANDLER_ERROR_PARAM, errorParam);

        eventRelay.sendEventMessage(new MotechEvent(
            createHandlerFailureSubject(task.getName(), e.getFailureCause()),
            params
        ));
    }

    private void handleSuccess(Map<String, Object> params, Task task) {
        LOGGER.debug("All actions from task: {} with ID: {} were successfully executed", task.getName(), task.getId());

        activityService.addSuccess(task);
        task.resetFailuresInRow();

        taskService.save(task, !task.getTrigger().getEffectiveListenerSubject().startsWith(SCHEDULER_TASK_TRIGGER_WILDCARD));

        eventRelay.sendEventMessage(new MotechEvent(
                createHandlerSuccessSubject(task.getName()),
                params
        ));
    }

    public void addDataProvider(DataProvider provider) {
        if (dataProviders == null) {
            dataProviders = new HashMap<>();
        }

        dataProviders.put(provider.getName(), provider);
    }

    public void removeDataProvider(String taskDataProviderId) {
        if (MapUtils.isNotEmpty(dataProviders)) {
            dataProviders.remove(taskDataProviderId);
        }
    }

    void setDataProviders(Map<String, DataProvider> dataProviders) {
        this.dataProviders = dataProviders;
    }

    private void publishTaskDisabledMessage(String taskName) {
        Map<String, Object> params = new HashMap<>();
        params.put("message", "Task disabled automatically: " + taskName);
        params.put("level", "CRITICAL");
        params.put("moduleName", settings.getBundleSymbolicName());

        eventRelay.sendEventMessage(new MotechEvent("org.motechproject.message", params));
    }

    private int getPossibleErrorsNumber() {
        String property = settings.getProperty(TASK_POSSIBLE_ERRORS_KEY);
        int number;

        try {
            number = Integer.parseInt(property);
        } catch (NumberFormatException e) {
            LOGGER.error(String.format(
                    "The value of key: %s is not a number. Possible errors number is set to zero.",
                    TASK_POSSIBLE_ERRORS_KEY
            ));
            number = 0;
        }

        return number;
    }

    @Autowired(required = false)
    public void setBundleContext(BundleContext bundleContext) {
        this.executor.setBundleContext(bundleContext);
    }
}
