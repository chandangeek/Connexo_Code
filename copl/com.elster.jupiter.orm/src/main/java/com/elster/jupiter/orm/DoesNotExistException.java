package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

/**
 * Thrown when an instance that should exist, is not found to.
 */
public class DoesNotExistException extends BaseException {

	public DoesNotExistException(String identification) {
		super(ExceptionTypes.DOES_NOT_EXIST, MessageFormat.format("Entity with identification {0} does not exist.", identification));
        set("identification", identification);
	}
}
