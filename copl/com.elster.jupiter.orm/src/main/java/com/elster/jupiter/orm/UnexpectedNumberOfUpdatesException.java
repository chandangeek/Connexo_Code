/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

/**
 * An insert, update or delete statment did not result in the expected number of updates.
 */
public class UnexpectedNumberOfUpdatesException extends PersistenceException {
	private static final long serialVersionUID = 1L;
	
    public enum Operation { INSERT, UPDATE, DELETE }

    public UnexpectedNumberOfUpdatesException(int expected, int actual, Operation operation) {
        super(MessageSeeds.UNEXPECTED_NUMBER_OF_UPDATES, expected, actual, operation);
        set("expected", expected);
        set("actual", actual);
        set("operation", operation);
    }
}
