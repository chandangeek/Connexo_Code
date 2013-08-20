package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

/**
 * Thrown when a query that should return at most one instance, returns more than one.
 */
public class NotUniqueException extends BaseException {

	public NotUniqueException(String identification) {
		super(ExceptionTypes.NOT_UNIQUE, MessageFormat.format("Not Unique by identification {0}", identification));
	}

}
