/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.rest.impl.MdcApplication;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    CHANGE_STATUS_CONCURRENT_TITLE(1, "ChangeStatusConcurrentTitle", "Failed to change status of ''{0}''", Level.SEVERE),
    CHANGE_STATUS_CONCURRENT_BODY(2, "ChangeStatusConcurrentMessage", "{0} has changed since the page was last updated.", Level.SEVERE),
    EDIT_POOL_CONCURRENT_TITLE(3, "EditPoolConcurrentTitle", "Failed to save ''{0}''", Level.SEVERE),
    EDIT_POOL_CONCURRENT_BODY(4, "EditPoolConcurrentMessage", "{0} has changed since the page was last updated.", Level.SEVERE),
    FIELD_REQUIRED(5, "FieldRequired", "This field is required", Level.SEVERE),
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
        return MdcApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }
}
