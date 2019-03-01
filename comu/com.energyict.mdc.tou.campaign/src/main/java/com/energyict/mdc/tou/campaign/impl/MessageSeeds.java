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
    DEVICE_BY_ID_NOT_FOUND(1005, "DeviceByIdNotFound", "Device By Id ''{0}'' Not Found"),
    COULD_NOT_FIND_SERVICE_CALL_TYPE(2001, "CouldNotFindServiceCallType", "Couldn''t find service call type {0} having version {1}."),
    UNABLE_TO_FIND_CALENDAR(277, "CannotFindCalendar", "Unable to find the given calendar in the system."),
    NO_ALLOWED_CALENDAR_DEVICE_MESSAGE(278, "NoAllowedCalendarMessage", "Unable to find an allowed calendar command with the given information"),
    SERVICE_CALL_PARENT_NOT_FOUND(279, "ServiceCallParentNotFound", "Service Call Parent Not Found"),

    MISSING_CONNECTION_TASKS(2001, "MissingConnectionTask", "Missing Connection Task"),
    DEVICE_NOT_CONTAINS_COMTASK_FOR_CALENDARS_OR_CONTAINS_ONLY_WRONG(2002, "DeviceNotContainsCommunicationTaskForCalendarsOrContainsOnlyWrong", "Device not contains communication task for calendars or contains only wrong"),
    DEVICE_NOT_CONTAINS_VERIFICATION_TASK_FOR_CALENDARS_OR_CONTAINS_ONLY_WRONG(2003, "DeviceNotContainsVerificationTaskForCalendarsOrContainsOnlyWrong", "Device not contains verification task for calendars or contains only wrong"),
    DEVICES_NOT_ADDED_BECAUSE_DIFFERENT_TYPE(2004, "DevicesNotAddedBecauseDifferentType", "''{0}'' devices were not added to the campaign because they are of a different type"),
    DEVICES_NOT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN(2005, "DevicesNotAddedBecausePartOtherCampaign", "''{0}'' devices were not added to the campaign because they are part of other ongoing campaigns"),
    DEVICES_NOT_ADDED_BECAUSE_HAVE_THIS_CALENDAR(2006, "DevicesNotAddedBecauseHaveThisCalendar", "''{0}'' devices were not added to the campaign because they already have this calendar"),
    CAMPAIGN_WAS_CANCELED_BECAUSE_DID_NOT_RECEIVE_DEVICES(2007, "CampaignWasCancelledBecauseDidNotReceiveDevices", "Campaign was cancelled because did not receive devices"),
    DEVICES_WITH_GROUP_AND_TYPE_NOT_FOUND(2008, "DevicesWithGroupAndTypeNotFound", "Devices with group ''{0}'' and type ''{1}'' not found"),
    CALENDAR_INSTALLATION_STARTED(2009, "CalendarInstallationStarted", "Calendar installation started"),
    CALENDAR_INSTALLATION_COMPLETED(2010, "CalendarInstallationCompleted", "Calendar installation completed"),
    CALENDAR_INSTALLATION_FAILED(2011, "CalendarInstallationFailed", "Calendar installation failed"),
    VERIFICATION_SCHEDULED(2012, "VerificationScheduled", "Verification scheduled"),
    VERIFICATION_COMPLETED(2013, "VerificationCompleted", "Verification completed"),
    VERIFICATION_FAILED(2014, "VerificationFailed", "Verification failed"),
    VERIFICATION_FAILED_WRONG_CALENDAR(2015, "VerificationFailedWrongCalendar", "Verification failed : wrong calendar"),
    ACTIVE_VERIFICATION_TASK_NOT_FOUND(2016, "ActiveVerificationTaskNotFound", "Active verification task not found"),

    CANCELED_BY_USER(3001, "CancelledByUser", "Cancelled by user"),
    RETRIED_BY_USER(3002, "RetriedByUser", "Retried by user");

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
