package com.elster.jupiter.events;

import com.elster.jupiter.util.exception.ExceptionType;

enum ExceptionTypes implements ExceptionType {
    CORRUPT_ACCESSPATH(1001), INVALID_PROPERTY_TYPE(1002), NO_SUCH_TOPIC(1003);
    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return "EVT";
    }

    @Override
    public int getNumber() {
        return number;
    }
}
