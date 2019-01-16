/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    DEVICE_NOT_CONTAIN_COMTASK_WITH_ACTIVITY_CALENDAR(5, "DeviceNotContainsComTaskWithActivitycalendar", "Device ''{0}'' NOT CONTAINS communication TASK WITH ACTIVITY CALENDAR"),
    DEVICE_GROUP_NOT_FOUND(1004, "DeviceGroupNotFound", "Device Group ''{0}'' Not Found"),
    DEVICE_BY_METER_ID_NOT_FOUND(1005, "DeviceByMeterIdNotFound", "Device By Meter Id ''{0}'' Not Found"),
    COULD_NOT_FIND_SERVICE_CALL_TYPE(2001, "CouldNotFindServiceCallType", "Couldn''t find service call type {0} having version {1}."),
    UNABLE_TO_FIND_CALENDAR(277, "CannotFindCalendar", "Unable to find the given calendar in the system."),
    NO_ALLOWED_CALENDAR_DEVICE_MESSAGE(278, "NoAllowedCalendarMessage", "Unable to find an allowed calendar command with the given information"),
    SERVICE_CALL_PARENT_NOT_FOUND(279, "ServiceCallParentNotFound", "Service Call Parent Not Found"),

    CANCELED_BY_USER(3001, "CancelledByUser","Cancelled by user"),
    RETRIED_BY_USER(3002, "RetriedByUser","Retried By User");
//    DEVI




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
        return TimeOfUseCampaignService.COMPONENT_NAME;
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

    public String code() {
        return String.valueOf(number);
    }

    public String translate(Thesaurus thesaurus, Object... args) {
        return thesaurus.getSimpleFormat(this).format(args);
    }

    public static final class Keys {
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String THIS_FIELD_IS_REQUIRED = "ThisFieldIsRequired";
        public static final String NAME_MUST_BE_UNIQUE = "NameMustBeUnique";
    }
}
