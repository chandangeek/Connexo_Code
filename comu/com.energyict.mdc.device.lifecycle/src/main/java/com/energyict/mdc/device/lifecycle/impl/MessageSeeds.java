/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import java.util.logging.Level;

/**
 * Defines the different error message that are produced by
 * this "device life cycle" bundle.
 */
public enum MessageSeeds implements MessageSeed {

    TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE(100, Keys.TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE, "Action ''{0}'' cannot be executed against the device (id={1}) because the device's life cycle does not support that action in its current state"),
    BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE(101, Keys.BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE, "Business process with deployment id ''{0}'' and process id ''{1}'' cannot be executed against the device (id={2}) because the device's life cycle does not support that action in its current state"),
    NOT_ALLOWED_2_EXECUTE(102, Keys.NOT_ALLOWED_2_EXECUTE, "The current user is not allowed to execute this action"),

    // MicroChecks
    MULTIPLE_MICRO_CHECKS_FAILED(10000, Keys.MULTIPLE_MICRO_CHECKS_FAILED, "Action cannot be triggered because the following checks have failed: {0}"),
    // Numbers 10001 - ... are reserved for com.energyict.mdc.device.lifecycle.impl.micro.checks.MicroCheckTranslations.Message

    // MicroActions
    MISSING_REQUIRED_PROPERTY_VALUES(20001, Keys.MISSING_REQUIRED_PROPERTY_VALUES, "No value was specified for the following property spec of the configured actions: {0}"),
    EFFECTIVE_TIMESTAMP_NOT_IN_RANGE(20002, Keys.EFFECTIVE_TIMESTAMP_NOT_IN_RANGE, "The transition date should be between {0} and {1}"),
    EFFECTIVE_TIMESTAMP_NOT_AFTER_LAST_STATE_CHANGE(20003, Keys.EFFECTIVE_TIMESTAMP_NOT_AFTER_LAST_STATE_CHANGE, "The transition date {1} should be after the last state change {2} for device (name={0})"),
    NOT_ALL_DATA_VALID_FOR_DEVICE(20004, "microAction.exception.notAllDataValidForDeviceX", "Device {0} has still suspect values: Action is undone."),
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
        return DeviceLifeCycleService.COMPONENT_NAME;
    }

    public static final class Keys {
        public static final String TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE = "transitionAction.not.device.current.state";
        public static final String BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE = "bpmAction.not.device.current.state";
        public static final String NOT_ALLOWED_2_EXECUTE = "authorizedAction.notAllowed2Execute";
        public static final String MULTIPLE_MICRO_CHECKS_FAILED = "authorizedAction.multiple.microChecksFailed";
        public static final String MISSING_REQUIRED_PROPERTY_VALUES = "authorizedAction.microAction.required.properties.multipleMissing";
        public static final String EFFECTIVE_TIMESTAMP_NOT_IN_RANGE = "authorizedAction.microAction.effectiveTimstamp.notInRange";
        public static final String EFFECTIVE_TIMESTAMP_NOT_AFTER_LAST_STATE_CHANGE = "authorizedAction.microAction.effectiveTimstamp.before.lastStateChange";
        public static final String EFFECTIVE_TIMESTAMP_NOT_AFTER_LAST_DATA = "authorizedAction.microAction.effectiveTimstamp.before.lastData";
    }
}
