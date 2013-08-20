package com.elster.jupiter.appserver;

import com.elster.jupiter.util.exception.ExceptionType;

enum ExceptionTypes implements ExceptionType {
    SERVER_MESSAGE_QUEUE_MISSING(1001), UNKOWN_APPSERVER_NAME(1002);

    private final int number;

    ExceptionTypes(int number) {
        this.number = number;
    }

    @Override
    public String getModule() {
        return "APS";
    }

    @Override
    public int getNumber() {
        return number;
    }
}
