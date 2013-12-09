package com.elster.jupiter.validation;

import com.elster.jupiter.util.exception.ExceptionType;

/**
 * Enumeration of all exception types in the VAL module.
 */
public enum ExceptionTypes implements ExceptionType {
    NO_SUCH_VALIDATOR(1001), MISSING_PROPERTY(1002);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return "VAL";
    }

    @Override
    public int getNumber() {
        return number;
    }
}

