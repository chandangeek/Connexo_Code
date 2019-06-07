/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    UNABLE_TO_CREATE_EVENT(2001, "ServiceCallIssueUnableToMapEvent", "Unable to create event from event payload: {0}", Level.INFO),
    DEVICE_TYPE_IN_USE(3001, "deviceTypeInUseByIssueCreationRule", "Device type ''{0}'' has one or more configurations that are still in use by an issue creation rule", Level.SEVERE),
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

    @Override
    public String getModule() {
        return IssueServiceCallService.COMPONENT_NAME;
    }

}