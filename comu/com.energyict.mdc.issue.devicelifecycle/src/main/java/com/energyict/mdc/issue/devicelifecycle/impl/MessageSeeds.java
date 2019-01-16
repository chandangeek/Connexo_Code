/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    UNABLE_TO_CREATE_EVENT(1, "DeviceLifecycleIssueUnableToMapEvent", "Unable to create event from event payload: {0}", Level.INFO),
    DEVICE_TYPE_IN_USE(2, "deviceTypeInUseByIssueCreationRule", "Device type ''{0}'' has one or more configurations that are still in use by an issue creation rule", Level.SEVERE),
    INVALID_NUMBER_OF_ARGUMENTS(3, "devicelifecycle.invalid.number.of.arguments", "Invalid number of arguments {0}, expected {1} ", Level.SEVERE),
    INVALID_ARGUMENT(4, "devicelifecycle.invalid.argument", "Invalid argument {0}", Level.SEVERE);

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
        return IssueDeviceLifecycleService.COMPONENT_NAME;
    }

}