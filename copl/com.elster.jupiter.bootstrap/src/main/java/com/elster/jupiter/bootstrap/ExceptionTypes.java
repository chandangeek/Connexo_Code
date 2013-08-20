package com.elster.jupiter.bootstrap;

import com.elster.jupiter.util.exception.ExceptionType;

enum ExceptionTypes implements ExceptionType {
    PROPERTY_NOT_FOUND(1001);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return "BTS";
    }

    @Override
    public int getNumber() {
        return number;
    }
}
