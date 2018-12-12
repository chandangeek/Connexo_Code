/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

enum ExceptionTypes implements MessageSeed {
    NESTED_TRANSACTION(1001, "Nested transactions are not allowed."),
    COMMIT_FAILED(1002, "Commit failed."),
    NOT_IN_TRANSACTION(1003, "A transaction related operation was performed outside of a transaction");

    private final int number;
    private final String defaultFormat;

    ExceptionTypes(int number, String defaultFormat) {
        this.number = number;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return "TRA";
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
