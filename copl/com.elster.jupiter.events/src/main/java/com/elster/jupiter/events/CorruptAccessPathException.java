package com.elster.jupiter.events;

import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when an access path cannot be resolved on any of the traveled beans.
 */
public class CorruptAccessPathException extends BaseException {

    public CorruptAccessPathException(Throwable cause) {
        super(ExceptionTypes.CORRUPT_ACCESSPATH, cause);
    }
}
