/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.i18n;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed {

    NO_APPSERVER(4, "NoAppServer", "There is no active application server that can handle this request"),
    NO_SUCH_MESSAGE_QUEUE(6, "NoSuchMessageQueue", "Unable to queue command: no message queue was found"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

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