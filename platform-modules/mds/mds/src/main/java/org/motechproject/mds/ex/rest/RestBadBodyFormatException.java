package org.motechproject.mds.ex.rest;

import org.motechproject.mds.ex.MdsException;

/**
 * Signals that there were errors parsing the class
 * from the provided body.
 */
public class RestBadBodyFormatException extends MdsException {

    private static final long serialVersionUID = -7809212543496212240L;

    public RestBadBodyFormatException(String message) {
        super(message);
    }

    public RestBadBodyFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
