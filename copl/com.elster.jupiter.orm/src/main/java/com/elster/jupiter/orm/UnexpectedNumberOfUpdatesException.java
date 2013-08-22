package com.elster.jupiter.orm;

import java.text.MessageFormat;

/**
 * An insert, update or delete statment did not result in the expected number of updates.
 */
public class UnexpectedNumberOfUpdatesException extends PersistenceException {

    public enum Operation { INSERT, UPDATE, DELETE }

    public UnexpectedNumberOfUpdatesException(int expected, int actual, Operation operation) {
        super(ExceptionTypes.UNEXPECTED_NUMBER_OF_UPDATES, MessageFormat.format("Expected {0} rows to be updated, yet {1} rows were updated for operation {2}.", expected, actual, operation));
        set("expected", expected);
        set("actual", actual);
        set("operation", operation);
    }
}
