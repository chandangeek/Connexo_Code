package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.BaseException;
import com.elster.jupiter.util.exception.ExceptionType;

/**
 * Abstract super class for all Persistence related Exceptions
 */
public abstract class PersistenceException extends BaseException {
	
	private static final long serialVersionUID = 1;

    protected PersistenceException(ExceptionType type, Throwable cause) {
        super(type, cause);
    }

    protected PersistenceException(ExceptionType type, String message) {
        super(type, message);
    }

    protected PersistenceException(ExceptionType type, String message, Throwable cause) {
        super(type, message, cause);
    }
}
