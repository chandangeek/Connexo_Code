package com.elster.jupiter.events;

import com.elster.jupiter.events.impl.Bus;
import com.elster.jupiter.util.exception.ExceptionType;

enum ExceptionTypes implements ExceptionType {
    CORRUPT_ACCESSPATH(1001), INVALID_PROPERTY_TYPE(1002);
    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return Bus.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return number;
    }
}
