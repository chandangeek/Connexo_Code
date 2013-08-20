package com.elster.jupiter.events;

import com.elster.jupiter.util.exception.BaseException;

public class CorruptAccessPathException extends BaseException {

    public CorruptAccessPathException(Throwable cause) {
        super(ExceptionTypes.CORRUPT_ACCESSPATH, cause);
    }
}
