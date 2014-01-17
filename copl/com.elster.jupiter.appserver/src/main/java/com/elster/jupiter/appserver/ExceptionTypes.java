package com.elster.jupiter.appserver;

import com.elster.jupiter.util.exception.ExceptionType;
import com.elster.jupiter.util.exception.MessageSeed;

enum ExceptionTypes implements ExceptionType {
    SERVER_MESSAGE_QUEUE_MISSING(MessageSeeds.SERVER_MESSAGE_QUEUE_MISSING),
    UNKOWN_APPSERVER_NAME(MessageSeeds.APPSERVER_NAME_UNKNOWN);

    private final MessageSeed seed;

    ExceptionTypes(MessageSeed seed) {
        this.seed = seed;
    }

    @Override
    public String getModule() {
        return "APS";
    }

    @Override
    public int getNumber() {
        return seed.getNumber();
    }
}
