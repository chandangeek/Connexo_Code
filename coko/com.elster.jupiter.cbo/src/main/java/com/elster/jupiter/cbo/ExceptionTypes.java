package com.elster.jupiter.cbo;

import com.elster.jupiter.util.exception.ExceptionType;

public enum ExceptionTypes implements ExceptionType {
    ILLEGAL_ENUM_VALUE(1001);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return "CBO";
    }

    @Override
    public int getNumber() {
        return number;
    }
}
