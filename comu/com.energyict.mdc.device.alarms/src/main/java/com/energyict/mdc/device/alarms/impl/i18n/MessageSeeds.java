/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.i18n;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.alarms.DeviceAlarmService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    EVENT_BAD_DATA_NO_DEVICE(1, "EventBadDataNoDevice", "Unable to process alarm creation event because target device (id = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_KORE_DEVICE(2, "EventBadDataNoEndDevice", "Unable to process alarm creation event because target kore device (amrId = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_TIMESTAMP(3, "EventBadDataNoTimestamp", "Unable to process alarm creation event because event timestamp cannot be obtained", Level.SEVERE),
    UNABLE_TO_CREATE_EVENT(4, "UnableToCreateEvent", "Unable to create event", Level.SEVERE),
    INVALID_NUMBER_OF_ARGUMENTS(9, "invalid.number.of.arguments", "Invalid number of arguments {0}, expected {1} ", Level.SEVERE),
    INVALID_ARGUMENT(10, "invalid.argument", "Invalid argument {0}", Level.SEVERE),
    INCORRECT_NUMBER_OF_CONCURRENT_PROCESSED_EVENTS(11, "incorrect.number.of.concurrent.processed.events", "Incorrect number of concurrently processed events : {0}", Level.SEVERE),
    DEVICE_TYPE_IN_USE(12, "deviceTypeInUseByAlarmCreationRule", "Device type ''{0}'' is still in use by an alarm creation rule", Level.SEVERE),
    DEVICE_GROUP_IN_USE(12, "deviceGroupInUseByAlarmCreationRule", "Device group ''{0}'' is still in use by an alarm creation rule", Level.SEVERE),
    RELATIVE_PERIOD_IN_USE(13, "relativePeriodInUseByAlarmCreationRule", "The relative period ''{0}'' is still in use by an alarm creation rule", Level.SEVERE),
    ALARM_RULE_STILL_HAS_ACTIVE_WEB_SERVICE(14, "AlarmRuleWithWebService", "The web service endpoint is still in use by an alarm creation rule.", Level.SEVERE),
    INCOMPLETE_MAIL_CONFIG(15, "mail.incomplete.config", "Mail configuration is incomplete. The following properties are missing: {0}",Level.SEVERE)
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
        return DeviceAlarmService.COMPONENT_NAME;
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