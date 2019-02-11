/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    DEVICE_DOESNT_CONTAIN_COMTASK_WITH_ACTIVITY_CALENDAR(5, "DeviceNotContainsComTaskWithActivityСalendar", "Device ''{0}'' doesn''t contain a communication TASK WITH ACTIVITY CALENDAR."),
    FIELD_TOO_LONG(6, "FieldTooLong", "Field too long."),
    THIS_FIELD_IS_REQUIRED(7, "ThisFieldIsRequired", "This field is required."),
    NAME_MUST_BE_UNIQUE(8, "NameMustBeUnique", "Name must be unique."),
    UNABLE_TO_FIND_CALENDAR(277, "CannotFindCalendar", "Unable to find the specified calendar in the system."),
    NO_ALLOWED_CALENDAR_DEVICE_MESSAGE(278, "NoAllowedCalendarMessage", "Unable to find an allowed calendar command with the given information."),
    SERVICE_CALL_PARENT_NOT_FOUND(279, "ServiceCallParentNotFound", "Parent service call isn''t found."),
    DEVICE_GROUP_NOT_FOUND(1004, "DeviceGroupNotFound", "Device group ''{0}'' not found."),
    DEVICE_WITH_METER_ID_NOT_FOUND(1005, "DeviceWithMeterIdNotFound", "Device with meter id ''{0}'' isn''t found."),
    DEVICE_WITH_ID_NOT_FOUND(1006, "DeviceWithIdNotFound", "Device with id ''{0}'' not found."),
    COULDNT_FIND_SERVICE_CALL_TYPE(1007, "CouldntFindServiceCallType", "Couldn''t find a service call type {0} having a version {1}."),
    MISSING_CONNECTION_TASKS(2001, "MissingConnectionTask", "Missing connection task."),
    DEVICE_DOESNT_CONTAIN_COMTASK_FOR_CALENDARS_OR_CONTAINS_ONLY_WRONG(2002, "DeviceNotContainsCommunicationTaskForCalendarsOrContainsOnlyWrong", "Device doesn''t contain a communication task for calendars or contains only wrong ones."),
    DEVICE_DOESNT_CONTAIN_VERIFICATION_TASK_FOR_CALENDARS_OR_CONTAINS_ONLY_WRONG(2003, "DeviceNotContainsVerificationTaskForCalendarsOrContainsOnlyWrong", "Device doesn't contain verification task for calendars or contains only wrong."),
    DEVICES_WERENT_ADDED_BECAUSE_DIFFERENT_TYPE(2004, "DevicesNotAddedBecauseDifferentType", "''{0}'' devices weren't added to the campaign because they are of a different type."),
    DEVICES_WERENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN(2005, "DevicesNotAddedBecausePartOtherCampaign", "''{0}'' devices weren't added to the campaign because they are a part of another ongoing campaign."),
    DEVICES_WERENT_ADDED_BECAUSE_HAVE_THIS_CALENDAR(2006, "DevicesNotAddedBecauseHaveThisCalendar", "''{0}'' devices weren't added to the campaign because they already have this calendar."),
    CAMPAIGN_WAS_CANCELED_BECAUSE_DIDNT_RECEIVE_DEVICES(2007, "CampaignWasCancelledBecauseDidNotReceiveDevices", "Campaign was cancelled because it didn''t receive devices."),
    DEVICES_WITH_GROUP_AND_TYPE_NOT_FOUND(2008, "DevicesWithGroupAndTypeNotFound", "Devices with group ''{0}'' and type ''{1}'' were not found."),
    CALENDAR_INSTALLATION_STARTED(2009, "CalendarInstallationStarted", "Calendar installation has started."),
    CALENDAR_INSTALLATION_COMPLETED(2010, "CalendarInstallationCompleted", "Calendar installation has been completed."),
    CALENDAR_INSTALLATION_FAILED(2011, "CalendarInstallationFailed", "Calendar installation has failed."),
    VERIFICATION_SCHEDULED(2012, "VerificationScheduled", "Verification has been scheduled."),
    VERIFICATION_COMPLETED(2013, "VerificationCompleted", "Verification has been completed."),
    VERIFICATION_FAILED(2014, "VerificationFailed", "Verification has failed."),
    VERIFICATION_FAILED_WRONG_CALENDAR(2015, "VerificationFailedWrongCalendar", "Verification has failed : wrong calendar."),
    ACTIVE_VERIFICATION_TASK_NOT_FOUND(2016, "ActiveVerificationTaskNotFound", "Active verification task not found."),
    ACTIVE_SERVICE_CALL_BY_DEVICE_NOT_FOUND(2017, "ActiveServiceCallByDeviceNotFound", "Active service call by device {0} not found."),
    DEVICE_BY_SERVICE_CALL_NOT_FOUND(2018, "DeviceByServiceCallNotFound", "Device by service call not found."),
    DEVICE_WAS_ADDED(2019, "DeviceWasAdded", "Device was added"),

    CANCELED_BY_USER(3001,"CancelledByUser","Cancelled by user."),

    RETRIED_BY_USER(3002,"RetriedByUser","Retried by user.");

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
