package com.elster.jupiter.util;

import com.elster.jupiter.util.exception.ExceptionType;

/**
 * Enumeration of the exception types in UTL module.
 */
public enum ExceptionTypes implements ExceptionType {
    NO_SUCH_PROPERTY(1001), BEAN_AVALUATION_FAILED(1002), INVALID_CRON_EXPRESSION(1003), JSON_DESERIALIZATION_FAILED(1004), JSON_SERIALIZATION_FAILED(1005), JSON_GENERATION_FAILED(1006);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return "UTL";
    }

    @Override
    public int getNumber() {
        return number;
    }
}
