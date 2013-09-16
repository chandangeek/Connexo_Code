package com.elster.jupiter.transaction;

import com.elster.jupiter.util.exception.ExceptionType;

enum ExceptionTypes implements ExceptionType {
    NESTED_TRANSACTION(1001), COMMIT_FAILED(1002);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return "TRA";
    }

    @Override
    public int getNumber() {
        return number;
    }
}
