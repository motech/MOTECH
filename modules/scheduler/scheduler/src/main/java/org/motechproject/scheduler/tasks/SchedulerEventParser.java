package org.motechproject.scheduler.tasks;

import org.motechproject.commons.api.TasksEventParser;
import org.motechproject.scheduler.constants.SchedulerConstants;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SchedulerEventParser implements TasksEventParser {

    public static final String PARSER_NAME = "SchedulerJobs";

    @Override
    public Map<String, Object> parseEventParameters(String eventSubject, Map<String, Object> eventParameters) {
        return eventParameters;
    }

    @Override
    public String parseEventSubject(String eventSubject, Map<String, Object> eventParameters) {
        return (String) eventParameters.get(SchedulerConstants.JOB_ID_KEY);
    }

    @Override
    public String getName() {
        return PARSER_NAME;
    }
}
