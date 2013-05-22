package org.motechproject.tasks.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.commons.api.DataProvider;
import org.motechproject.commons.api.MotechException;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventListener;
import org.motechproject.event.listener.EventListenerRegistryService;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListenerEventProxy;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.tasks.domain.ActionEvent;
import org.motechproject.tasks.domain.ActionParameter;
import org.motechproject.tasks.domain.KeyInformation;
import org.motechproject.tasks.domain.Task;
import org.motechproject.tasks.domain.TaskActionInformation;
import org.motechproject.tasks.domain.TaskAdditionalData;
import org.motechproject.tasks.domain.TriggerEvent;
import org.motechproject.tasks.ex.ActionNotFoundException;
import org.motechproject.tasks.ex.TaskHandlerException;
import org.motechproject.tasks.ex.TriggerNotFoundException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

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
import static org.motechproject.tasks.events.constants.TaskFailureCause.ACTION;
import static org.motechproject.tasks.events.constants.TaskFailureCause.DATA_SOURCE;
import static org.motechproject.tasks.events.constants.TaskFailureCause.FILTER;
import static org.motechproject.tasks.events.constants.TaskFailureCause.TRIGGER;
import static org.motechproject.tasks.service.HandlerUtil.checkFilters;
import static org.motechproject.tasks.service.HandlerUtil.convertTo;
import static org.motechproject.tasks.service.HandlerUtil.findAdditionalData;
import static org.motechproject.tasks.service.HandlerUtil.getFieldValue;
import static org.motechproject.tasks.service.HandlerUtil.getTriggerKey;
import static org.springframework.util.ReflectionUtils.findMethod;

@Service
public class TaskTriggerHandler {
    private static final String TASK_POSSIBLE_ERRORS_KEY = "task.possible.errors";
    private static final int JOIN_PATTERN_BEGIN_INDEX = 5;
    private static final int DATETIME_PATTERN_BEGIN_INDEX = 9;

    private static final Logger LOG = LoggerFactory.getLogger(TaskTriggerHandler.class);

    private TaskService taskService;
    private TaskActivityService activityService;
    private EventListenerRegistryService registryService;
    private EventRelay eventRelay;
    private SettingsFacade settingsFacade;
    private Map<String, DataProvider> dataProviders;
    private BundleContext bundleContext;

    @Autowired
    public TaskTriggerHandler(TaskService taskService, TaskActivityService activityService,
                              EventListenerRegistryService registryService, EventRelay eventRelay,
                              SettingsFacade settingsFacade) {
        this.taskService = taskService;
        this.activityService = activityService;
        this.registryService = registryService;
        this.eventRelay = eventRelay;
        this.settingsFacade = settingsFacade;

        registerHandler();
    }

    public final void registerHandlerFor(String subject) {
        String serviceName = "taskTriggerHandler";
        Method method = findMethod(this.getClass(), "handle", MotechEvent.class);
        Object obj = CollectionUtils.find(registryService.getListeners(subject), new HandlerPredicate(serviceName));

        try {
            if (method != null && obj == null) {
                EventListener proxy = new MotechListenerEventProxy(serviceName, this, method);

                registryService.registerListener(proxy, subject);
                LOG.info(String.format("Register TaskTriggerHandler for subject: '%s'", subject));
            }
        } catch (Exception e) {
            LOG.error(String.format("Cant register TaskTriggerHandler for subject: %s", subject), e);
        }
    }

    public void handle(MotechEvent triggerEvent) {
        TriggerEvent trigger = getTriggerEvent(triggerEvent.getSubject());

        if (trigger != null) {
            try {
                for (Task task : selectTasks(trigger, triggerEvent.getParameters())) {
                    try {
                        for (TaskActionInformation actionInformation : task.getActions()) {
                            ActionEvent action = getActionEvent(actionInformation);
                            Map<String, Object> parameters = createParameters(task, actionInformation, action, triggerEvent);

                            executeAction(task, action, parameters);
                        }

                        registerSuccess(triggerEvent, task);
                    } catch (TaskHandlerException e) {
                        registerError(triggerEvent, task, e);
                    } catch (Exception e) {
                        TaskHandlerException exp = new TaskHandlerException(TRIGGER, "error.unrecognizedError", e);
                        registerError(triggerEvent, task, exp);
                    }
                }
            } catch (TaskHandlerException e) {
                sendFailureEvent(triggerEvent, null, e, 0);
            }
        }
    }

    private void registerHandler() {
        if (taskService != null) {
            List<Task> tasks = taskService.getAllTasks();

            for (Task t : tasks) {
                registerHandlerFor(t.getTrigger().getSubject());
            }
        }
    }

    private TriggerEvent getTriggerEvent(String subject) {
        TriggerEvent trigger = null;

        try {
            trigger = taskService.findTrigger(subject);
            LOG.info("Found trigger for subject: " + subject);
        } catch (TriggerNotFoundException e) {
            LOG.error(e.getMessage(), e);
        }

        return trigger;
    }

    private ActionEvent getActionEvent(TaskActionInformation actionInformation) throws TaskHandlerException {
        ActionEvent action;

        try {
            action = taskService.getActionEventFor(actionInformation);
            LOG.info("Found action on the basic of information: " + actionInformation);
        } catch (ActionNotFoundException e) {
            throw new TaskHandlerException(TRIGGER, "error.actionNotFound", e);
        }

        return action;
    }

    private List<Task> selectTasks(TriggerEvent trigger, Map<String, Object> triggerParameters) throws TaskHandlerException {
        List<Task> tasks = new ArrayList<>();

        for (Task task : taskService.findTasksForTrigger(trigger)) {
            try {
                if (task.isEnabled() && checkFilters(task.getFilters(), triggerParameters)) {
                    tasks.add(task);
                }
            } catch (Exception e) {
                throw new TaskHandlerException(FILTER, "error.filterError", e);
            }
        }

        return tasks;
    }

    private void executeAction(Task task, ActionEvent action, Map<String, Object> parameters) throws TaskHandlerException {
        boolean invokeMethod = action.hasService() && bundleContext != null;
        boolean serviceAvailable = false;

        if (invokeMethod) {
            serviceAvailable = callActionServiceMethod(action, new MethodHandler(action, parameters));

            if (!serviceAvailable) {
                activityService.addWarning(task, "warning.serviceUnavailable", action.getServiceInterface());
            }
        }

        boolean sendEvent = (!invokeMethod || !serviceAvailable) && action.hasSubject();

        if (sendEvent) {
            eventRelay.sendEventMessage(new MotechEvent(action.getSubject(), parameters));
        }

        if ((!invokeMethod || !serviceAvailable) && !sendEvent) {
            throw new TaskHandlerException(ACTION, "error.cantExecuteAction");
        }
    }

    private boolean callActionServiceMethod(ActionEvent action, MethodHandler methodHandler) throws TaskHandlerException {
        ServiceReference reference = bundleContext.getServiceReference(action.getServiceInterface());
        boolean serviceAvailable = reference != null;

        if (serviceAvailable) {
            Object service = bundleContext.getService(reference);
            String serviceMethod = action.getServiceMethod();
            Class[] classes = methodHandler.isParametrized() ? methodHandler.getClasses() : null;
            Object[] objects = methodHandler.isParametrized() ? methodHandler.getObjects() : null;

            try {
                Method method = service.getClass().getMethod(serviceMethod, classes);

                try {
                    method.invoke(service, objects);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new TaskHandlerException(ACTION, "error.serviceMethodInvokeError", e, serviceMethod, action.getServiceInterface());
                }
            } catch (NoSuchMethodException e) {
                throw new TaskHandlerException(ACTION, "error.notFoundMethodForService", e, serviceMethod, action.getServiceInterface());
            }
        }

        return serviceAvailable;
    }

    private Map<String, Object> createParameters(Task task, TaskActionInformation info, ActionEvent action, MotechEvent event) throws TaskHandlerException {
        SortedSet<ActionParameter> actionParameters = action.getActionParameters();
        Map<String, Object> parameters = new HashMap<>(actionParameters.size());

        for (ActionParameter param : actionParameters) {
            String key = param.getKey();

            if (!info.getValues().containsKey(key)) {
                throw new TaskHandlerException(TRIGGER, "error.taskActionNotContainsField", action.getDisplayName(), key);
            }

            String template = info.getValues().get(key);

            if (template == null) {
                throw new TaskHandlerException(TRIGGER, "error.templateNull", key, action.getDisplayName());
            }

            switch (param.getType()) {
                case LIST:
                    parameters.put(key, convertToList(task, event, template));
                    break;
                case MAP:
                    parameters.put(key, convertToMap(task, event, template));
                    break;
                default:
                    try {
                        String userInput = replaceAll(template, event, task);
                        Object obj = convertTo(param.getType(), userInput);
                        parameters.put(key, obj);
                    } catch (MotechException ex) {
                        throw new TaskHandlerException(TRIGGER, ex.getMessage(), ex, key);
                    }
            }
        }

        return parameters;
    }

    private Map<Object, Object> convertToMap(Task task, MotechEvent event, String template) throws TaskHandlerException {
        String[] rows = template.split("(\\r)?\\n");
        Map<Object, Object> tempMap = new HashMap<>(rows.length);

        for (String row : rows) {
            String[] array = row.split(":");
            Object mapKey;
            Object mapValue;

            switch (array.length) {
                case 2:
                    mapKey = getValue(array[0], event, task);
                    mapValue = getValue(array[1], event, task);

                    tempMap.put(mapKey, mapValue);
                    break;
                case 1:
                    mapValue = getValue(array[0], event, task);

                    tempMap.putAll((Map) mapValue);
                    break;
                default:
            }
        }
        return tempMap;
    }

    private List<Object> convertToList(Task task, MotechEvent event, String template) throws TaskHandlerException {
        String[] rows = template.split("(\\r)?\\n");
        List<Object> tempList = new ArrayList<>(rows.length);

        for (String row : rows) {
            Object value = getValue(row, event, task);

            if (value instanceof Collection) {
                tempList.addAll((Collection) value);
            } else {
                tempList.add(value);
            }
        }
        return tempList;
    }

    private Object getValue(String row, MotechEvent event, Task task) throws TaskHandlerException {
        List<KeyInformation> keys = KeyInformation.parseAll(row);
        Object result = null;

        if (keys.isEmpty()) {
            result = row;
        } else {
            KeyInformation rowKeyInfo = keys.get(0);

            if (rowKeyInfo.fromTrigger()) {
                result = event.getParameters().get(rowKeyInfo.getKey());
            } else if (rowKeyInfo.fromAdditionalData()) {
                result = getAdditionalDataValue(event, task, rowKeyInfo);
            }
        }

        return result;
    }

    private String replaceAll(String template, MotechEvent event, Task task) throws TaskHandlerException {
        String conversionTemplate = template;

        for (KeyInformation key : KeyInformation.parseAll(template)) {
            String value = "";

            if (key.fromTrigger()) {
                try {
                    value = getTriggerKey(event, key);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new TaskHandlerException(TRIGGER, "error.objectNotContainsField", e, key.getKey());
                }
            } else if (key.fromAdditionalData()) {
                value = getAdditionalDataValue(event, task, key).toString();
            }

            String replacedValue = !key.getManipulations().isEmpty() ? manipulateValue(value, key.getManipulations(), task) : value;
            conversionTemplate = conversionTemplate.replace(String.format("{{%s}}", key.getOriginalKey()), replacedValue);
        }

        return conversionTemplate;
    }

    private Object getAdditionalDataObject(MotechEvent event, Task task, KeyInformation key) throws TaskHandlerException {
        if (dataProviders == null || dataProviders.isEmpty()) {
            throw new TaskHandlerException(DATA_SOURCE, "error.notFoundDataProvider", key.getObjectType());
        }

        DataProvider provider = dataProviders.get(key.getDataProviderId());

        if (provider == null) {
            throw new TaskHandlerException(DATA_SOURCE, "error.notFoundDataProvider", key.getObjectType());
        }

        TaskAdditionalData ad = findAdditionalData(task, key);
        String value = replaceAll(ad.getLookupValue(), event, task);

        Map<String, String> lookupFields = new HashMap<>();
        lookupFields.put(ad.getLookupField(), value);

        return provider.lookup(key.getObjectType(), lookupFields);
    }

    private Object getAdditionalDataValue(MotechEvent event, Task task, KeyInformation key) throws TaskHandlerException {
        TaskAdditionalData ad = findAdditionalData(task, key);
        Object found = getAdditionalDataObject(event, task, key);
        Object value;

        if (found == null) {
            if (ad.isFailIfDataNotFound()) {
                throw new TaskHandlerException(DATA_SOURCE, "error.notFoundObjectForType", key.getObjectType());
            } else {
                activityService.addWarning(task, "warning.notFoundObjectForType", key.getObjectType());
            }
        }

        try {
            value = getFieldValue(found, key.getKey());
        } catch (Exception e) {
            if (ad.isFailIfDataNotFound()) {
                throw new TaskHandlerException(DATA_SOURCE, "error.objectNotContainsField", e, key.getKey());
            } else {
                activityService.addWarning(task, "warning.objectNotContainsField", key.getKey(), e);
                value = "";
            }
        }

        return value;
    }

    private void registerError(MotechEvent trigger, Task task, TaskHandlerException e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Omitted task with ID: %s because: ", task.getId()), e);
        }

        activityService.addError(task, e);

        int failureNumber = activityService.errorsFromLastRun(task).size();
        int possibleErrorRun = Integer.valueOf(settingsFacade.getProperty(TASK_POSSIBLE_ERRORS_KEY));

        if (failureNumber >= possibleErrorRun) {
            task.setEnabled(false);
            taskService.save(task);

            activityService.addWarning(task);
            publishTaskDisabledMessage(task.getName());
        }

        sendFailureEvent(trigger, task, e, failureNumber);
    }

    private void registerSuccess(MotechEvent trigger, Task task) {
        activityService.addSuccess(task);

        sendEvent(createHandlerSuccessSubject(task.getName()), trigger.getParameters());
    }

    private String manipulateValue(String value, List<String> manipulations, Task task) throws TaskHandlerException {
        String manipulateValue = value;

        for (String manipulation : manipulations) {
            String lowerCase = manipulation.toLowerCase();

            if (!lowerCase.startsWith("join") && !lowerCase.startsWith("datetime")) {
                switch (lowerCase) {
                    case "toupper":
                        manipulateValue = manipulateValue.toUpperCase();
                        break;
                    case "tolower":
                        manipulateValue = manipulateValue.toLowerCase();
                        break;
                    case "capitalize":
                        manipulateValue = WordUtils.capitalize(manipulateValue);
                        break;
                    default:
                        activityService.addWarning(task, "warning.manipulation", manipulation);
                        break;
                }
            } else if (lowerCase.contains("join")) {
                String[] splitValue = manipulateValue.split(" ");
                String pattern = manipulation.substring(JOIN_PATTERN_BEGIN_INDEX, manipulation.length() - 1);

                manipulateValue = StringUtils.join(splitValue, pattern);
            } else if (lowerCase.contains("datetime")) {
                try {
                    String pattern = manipulation.substring(DATETIME_PATTERN_BEGIN_INDEX, manipulation.length() - 1);
                    DateTimeFormatter format = DateTimeFormat.forPattern(pattern);

                    manipulateValue = format.print(new DateTime(manipulateValue));
                } catch (IllegalArgumentException e) {
                    throw new TaskHandlerException(TRIGGER, "error.date.format", e, manipulation);
                }
            } else {
                activityService.addWarning(task, "warning.manipulation", manipulation);
            }
        }

        return manipulateValue;
    }

    public void addDataProvider(String taskDataProviderId, DataProvider provider) {
        if (dataProviders == null) {
            dataProviders = new HashMap<>();
        }

        dataProviders.put(taskDataProviderId, provider);
    }

    public void removeDataProvider(String taskDataProviderId) {
        if (dataProviders != null && !dataProviders.isEmpty()) {
            dataProviders.remove(taskDataProviderId);
        }
    }

    void setDataProviders(Map<String, DataProvider> dataProviders) {
        this.dataProviders = dataProviders;
    }

    private Map<String, Object> createErrorParameters(Task task, TaskHandlerException e, int failureNumber) {
        Map<String, Object> param = new HashMap<>();
        param.put(TASK_FAIL_MESSAGE, e.getMessage());
        param.put(TASK_FAIL_STACK_TRACE, ExceptionUtils.getStackTrace(e));
        param.put(TASK_FAIL_FAILURE_DATE, DateTime.now());

        if (task != null) {
            param.put(TASK_FAIL_FAILURE_NUMBER, failureNumber);
            param.put(TASK_FAIL_TRIGGER_DISABLED, task.isEnabled());
            param.put(TASK_FAIL_TASK_ID, task.getId());
            param.put(TASK_FAIL_TASK_NAME, task.getName());
        }

        return param;
    }

    private void publishTaskDisabledMessage(String taskName) {
        Map<String, Object> params = new HashMap<>();
        params.put("message", "Task disabled automatically: " + taskName);
        params.put("level", "CRITICAL");
        params.put("moduleName", "tasks");

        MotechEvent motechEvent = new MotechEvent("org.motechproject.message", params);

        eventRelay.sendEventMessage(motechEvent);
    }

    private void sendFailureEvent(MotechEvent trigger, Task task, TaskHandlerException e, int failureNumber) {
        Map<String, Object> param = trigger.getParameters();
        param.put(HANDLER_ERROR_PARAM, createErrorParameters(task, e, failureNumber));

        sendEvent(createHandlerFailureSubject(task.getName(), e.getFailureCause()), param);
    }

    private void sendEvent(String subject, Map<String, Object> parameters) {
        eventRelay.sendEventMessage(new MotechEvent(subject, parameters));
    }

    @Autowired(required = false)
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
