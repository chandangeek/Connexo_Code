package com.elster.jupiter.messaging;

import com.elster.jupiter.util.exception.ExceptionType;

/**
 * Enumeration of all exception types in the MSG module.
 */
public enum ExceptionTypes implements ExceptionType {
    CANNOT_SUBSCRIBE_ON_INACTIVE_DESTINATION(1001), DUPLICATE_SUBSCRIBER_NAME(1002), MULTIPLE_SUBSCRIBER_ON_QUEUE(1003), UNDERLYING_JMS_EXCEPTION(1004), UNDERLYING_AQ_EXCEPTION(1005);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return "MSG";
    }

    @Override
    public int getNumber() {
        return number;
    }
}
