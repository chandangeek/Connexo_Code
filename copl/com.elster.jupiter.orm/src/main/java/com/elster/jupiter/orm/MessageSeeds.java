/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

enum MessageSeeds implements MessageSeed {
    DOES_NOT_EXIST(1001, "Entity with identification {0} does not exist."),
    NOT_UNIQUE(1002, "Not Unique by identification {0}"),
    SQL(1003, "Underlying SQL failed"),
    OPTIMISTIC_LOCK(1004, "Optimistic lock failed."),
    TRANSACTION_REQUIRED(1005, "Transaction required for this operation."),
    MAPPING_INTROSPECTION_FAILED(1006, "Mapping introspection failed."),
    MAPPING_MISMATCH(1007, "Mapping mismatch"),
    MAPPING_MISMATCH_FOR_CLASS(1008, "No mapping found for class {0}"),
    MAPPING_MISMATCH_FOR_FIELD(1009, "No mapping found for field {1} on class {0}"),
    NO_MAPPING_FOR_SQL_TYPE(1010, "No mapping found for SQL type {0}"),
    MAPPING_NO_DISCRIMINATOR_COLUMN(1011, "No discriminator column found."),
    UNEXPECTED_NUMBER_OF_UPDATES(1012, "Expected {0} rows to be updated, yet {1} rows were updated for operation {2}."),
    UNDERLYING_IO_EXCEPTION(1013, "Underlying IO Exception");

    private final int number;
    private final String defaultFormat;

    MessageSeeds(int number, String defaultFormat) {
        this.number = number;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return OrmService.COMPONENTNAME;
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