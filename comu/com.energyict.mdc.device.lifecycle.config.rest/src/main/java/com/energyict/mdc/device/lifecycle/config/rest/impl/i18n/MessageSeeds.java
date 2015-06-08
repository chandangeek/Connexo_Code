package com.energyict.mdc.device.lifecycle.config.rest.impl.i18n;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.rest.impl.DeviceLifeCycleConfigApplication;

import java.text.MessageFormat;
import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed, TranslationKey {

    DEVICE_LIFECYCLE_NOT_FOUND(1, "device.lifecycle.not.found", "Device lifecycle with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_NOT_FOUND(2, "device.lifecycle.state.not.found", "Device lifecycle state with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_AUTH_ACTION_NOT_FOUND(3, "device.lifecycle.auth.action.not.found", "Authorized action with id '{0}' doesn't exist", Level.SEVERE),
    DEVICE_LIFECYCLE_EVENT_TYPE_NOT_FOUND(4, "device.lifecycle.event.type.not.found", "Event type with symbol '{0}' doesn't exist", Level.SEVERE),
    FIELD_CAN_NOT_BE_EMPTY(5, "field.cn.not.be.empty", "This field is required", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_IS_STILL_USED_BY_TRANSITIONS(6, "unable.to.remove.state.with.transitions", "This state cannot be removed from this device life cycle because it is used on transitions: {0}", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_IS_THE_LATEST_STATE(7, "unable.to.remove.latest.state", "This state cannot be removed from this device life cycle because it's the latest state. Add another state first.", Level.SEVERE),
    DEVICE_LIFECYCLE_STATE_IS_THE_INITIAL_STATE(8, "unable.to.remove.initial.state", "This state cannot be removed from this device life cycle because it's the initial state. Set another state as initial state first", Level.SEVERE),
    DEVICE_LIFECYCLE_IS_USED_BY_DEVICE_TYPE(9, "device.lifecycle.is.used.by.device.type", "This operation cannot be performed for this device life cycle because one or more devices types use this device life cycle.", Level.SEVERE);

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

    public String getFormatted(Object... args){
        return MessageFormat.format(this.getDefaultFormat(), args);
    }

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args){
        String text = thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat());
        return MessageFormat.format(text, args);
    }


    public static MessageSeeds getByKey(String key) {
        if (key != null) {
            for (MessageSeeds column : MessageSeeds.values()) {
                if (column.getKey().equals(key)) {
                    return column;
                }
            }
        }
        return null;
    }
}
