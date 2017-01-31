/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.i18n;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.rest.impl.DeviceLifeCycleConfigApplication;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    DEVICE_LIFECYCLE_NOT_FOUND(1, "device.lifecycle.not.found", "Device lifecycle with id ''{0}'' doesn't exist"),
    DEVICE_LIFECYCLE_STATE_NOT_FOUND(2, "device.lifecycle.state.not.found", "Device lifecycle state with id ''{0}'' doesn't exist"),
    DEVICE_LIFECYCLE_AUTH_ACTION_NOT_FOUND(3, "device.lifecycle.auth.action.not.found", "Authorized action with id ''{0}'' doesn't exist"),
    DEVICE_LIFECYCLE_EVENT_TYPE_NOT_FOUND(4, "device.lifecycle.event.type.not.found", "Event type with symbol ''{0}'' doesn't exist"),
    FIELD_CAN_NOT_BE_EMPTY(5, "field.cn.not.be.empty", "This field is required"),
    DEVICE_LIFECYCLE_STATE_IS_STILL_USED_BY_TRANSITIONS(6, "unable.to.remove.state.with.transitions", "This state cannot be removed from this device life cycle because it is used on transitions: {0}."),
    DEVICE_LIFECYCLE_STATE_IS_THE_LATEST_STATE(7, "unable.to.remove.latest.state", "This state cannot be removed from this device life cycle because it is the latest state. Add another state first."),
    DEVICE_LIFECYCLE_STATE_IS_THE_INITIAL_STATE(8, "unable.to.remove.initial.state", "This state cannot be removed from this device life cycle because it is the initial state. Set another state as initial state first."),
    DEVICE_LIFECYCLE_IS_USED_BY_DEVICE_TYPE(9, "device.lifecycle.is.used.by.device.type", "This operation cannot be performed for this device life cycle because one or more device types use this device life cycle."),
    STATE_CHANGE_BUSINESS_PROCESS_NOT_FOUND(10, "device.lifecycle.state.process.not.found", "State change business process ''{0}'' doesn't exist");

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
        return DeviceLifeCycleConfigApplication.DEVICE_CONFIG_LIFECYCLE_COMPONENT;
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