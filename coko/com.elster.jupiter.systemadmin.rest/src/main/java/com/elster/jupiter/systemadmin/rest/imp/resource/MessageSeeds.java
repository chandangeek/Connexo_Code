/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.systemadmin.rest.imp.SystemApplication;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum  MessageSeeds implements MessageSeed {

    PURGE_HISTORY_DOES_NOT_EXIST(1, "PurgeHistoryDoesNotExist", "The purge history record with id = ''{0}'' doesn't exist", Level.WARNING),
    INVALID_LICENSE_FILE(2, "InvalidLicenseFile", "Invalid license file", Level.SEVERE),
    MAX_FILE_SIZE_EXCEEDED(13, "MaxFileSizeExceeded", "File size should be less than {0} Mb", Level.SEVERE),
    FILE_IO(4, "FileIO", "Failure while doing IO on file", Level.SEVERE),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return SystemApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

}
