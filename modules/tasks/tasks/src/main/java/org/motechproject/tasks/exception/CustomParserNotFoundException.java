package org.motechproject.tasks.exception;

import org.motechproject.commons.api.TasksEventParser;

import static java.lang.String.format;

/**
 * Indicates an error, while looking for the Custom Event Parser in the context.
 */
public class CustomParserNotFoundException extends IllegalArgumentException {

    /**
     * Exception constructor.
     *
     * @param parserName  the name of the missing parser
     */
    public CustomParserNotFoundException(String parserName) {
        super(format("The parser with name %s has not been found. This might mean that you did not " +
                "expose an implementation of the " + TasksEventParser.class.getName() + ", that returns " +
                "the aforementioned parser name calling getName(), as an OSGi service", parserName));
    }
}
