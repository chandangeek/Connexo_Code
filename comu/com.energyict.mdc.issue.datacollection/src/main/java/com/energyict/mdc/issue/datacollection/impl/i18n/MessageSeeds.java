/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.i18n;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    EVENT_BAD_DATA_NO_DEVICE(1, "EventBadDataNoDevice", "Unable to process issue creation event because target device (id = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_KORE_DEVICE(2, "EventBadDataNoEndDevice", "Unable to process issue creation event because target kore device (amrId = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_TIMESTAMP(3, "EventBadDataNoTimestamp", "Unable to process issue creation event because event timestamp cannot be obtained", Level.SEVERE),
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
        return IssueDataCollectionService.COMPONENT_NAME;
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