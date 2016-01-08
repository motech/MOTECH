package org.motechproject.mds.ex.entity;

import org.motechproject.mds.ex.MdsException;

/**
 * This exception signals that an Entity was changed (presumably by another user).
 */
public class EntityChangedException extends MdsException {

    private static final long serialVersionUID = -8535112651702892785L;

    public EntityChangedException() {
        super("Unable to save entity changes, it was already changed", null, "mds.error.entityChanged");
    }
}
