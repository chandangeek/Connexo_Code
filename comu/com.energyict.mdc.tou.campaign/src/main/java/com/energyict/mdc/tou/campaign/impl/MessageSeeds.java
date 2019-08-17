/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    DEVICE_DOESNT_CONTAIN_COMTASK_WITH_ACTIVITY_CALENDAR(5, "DeviceDoesntContainsComTaskWithActivityСalendar", "Device ''{0}'' doesn''t contain a communication task with activity calendar."),
    FIELD_TOO_LONG(6, "FieldTooLong", "Field too long."),
    THIS_FIELD_IS_REQUIRED(7, "ThisFieldIsRequired", "This field is required."),
    NAME_MUST_BE_UNIQUE(8, "NameMustBeUnique", "Name must be unique."),
    UNABLE_TO_FIND_CALENDAR(277, "CannotFindCalendar", "Unable to find the specified calendar in the system."),
    NO_ALLOWED_CALENDAR_DEVICE_MESSAGE(278, "NoAllowedCalendarMessage", "Unable to find an allowed calendar command with the given information."),
    SERVICE_CALL_PARENT_NOT_FOUND(279, "ServiceCallParentNotFound", "Parent service call isn''t found."),
    DEVICE_GROUP_ISNT_FOUND(1004, "DeviceGroupIsntFound", "Device group ''{0}'' isn''t found."),
    DEVICE_WITH_METER_ID_ISNT_FOUND(1005, "DeviceWithMeterIdIsntFound", "Device with meter id ''{0}'' isn''t found."),
    DEVICE_WITH_ID_ISNT_FOUND(1006, "DeviceWithIdIsntFound", "Device with id ''{0}'' isn''t found."),
    COULDNT_FIND_SERVICE_CALL_TYPE(1007, "CouldntFindServiceCallType", "Couldn''t find a service call type {0} with version {1}."),
    MISSING_CONNECTION_TASKS(2001, "MissingConnectionTask", "A connection method is missing."),
    DEVICES_WERENT_ADDED_BECAUSE_DIFFERENT_TYPE(2004, "DevicesWerentAddedBecauseDifferentType", "''{0}'' devices weren''t added to the campaign because they are of a different type."),
    DEVICES_WERENT_ADDED_BECAUSE_PART_OTHER_CAMPAIGN(2005, "DevicesWerentAddedBecausePartOtherCampaign", "''{0}'' devices weren''t added to the campaign because they are a part of another ongoing campaign."),
    DEVICES_WERENT_ADDED_BECAUSE_HAVE_THIS_CALENDAR(2006, "DevicesWerentAddedBecauseHaveThisCalendar", "''{0}'' devices weren''t added to the campaign because they already have this calendar."),
    CAMPAIGN_WAS_CANCELED_BECAUSE_DIDNT_RECEIVE_DEVICES(2007, "CampaignWasCancelledBecauseDidNotReceiveDevices", "Campaign was cancelled because it didn''t receive devices."),
    DEVICES_WITH_GROUP_AND_TYPE_NOT_FOUND(2008, "DevicesWithGroupAndTypeNotFound", "Devices with group ''{0}'' and type ''{1}'' were''nt found."),
    CALENDAR_INSTALLATION_STARTED(2009, "CalendarInstallationStarted", "Calendar installation has started."),
    CALENDAR_INSTALLATION_COMPLETED(2010, "CalendarInstallationCompleted", "Calendar installation has been completed."),
    CALENDAR_INSTALLATION_FAILED(2011, "CalendarInstallationFailed", "Calendar installation has failed."),
    VERIFICATION_SCHEDULED(2012, "VerificationScheduled", "Verification has been scheduled."),
    VERIFICATION_COMPLETED(2013, "VerificationCompleted", "Verification has been completed."),
    VERIFICATION_FAILED(2014, "VerificationFailed", "Verification has failed."),
    VERIFICATION_FAILED_WRONG_CALENDAR(2015, "VerificationFailedWrongCalendar", "Verification has failed: another calendar is installed on device."),
    ACTIVE_VERIFICATION_TASK_ISNT_FOUND(2016, "ActiveVerificationTaskIsntFound", "Active verification task isn''t found."),
    ACTIVE_SERVICE_CALL_HASNT_FOUND_BY_DEVICE_NOT(2017, "ActiveServiceCallHasntFoundByDevice", "Active service call hasn''t been found by device {0}"),
    DEVICE_BY_SERVICE_CALL_NOT_FOUND(2018, "DeviceByServiceCallNotFound", "Device by service call not found."),
    DEVICE_WAS_ADDED(2019, "DeviceWasAdded", "Device was added"),
    TOU_ITEM_WITH_ID_ISNT_FOUND(2020, "TimeOfUseCampaignItemWithIdIsntFound", "Time of use campaign item with id {0} isn''t found."),
    TOU_CAMPAIGN_WITH_ID_ISNT_FOUND(2021, "TimeOfUseCampaignWithIdIsntFound", "Time of use campaign with id {0} isn''t found."),
    TASK_FOR_VALIDATION_IS_MISSING(2022, "TaskForValidationIsMissing", "Communication task ''{0}'' required for validation is missing on the device configuration, doesn''t meet the necessary conditions or is inactive on device/device type level", Level.SEVERE),
    TASK_FOR_SENDING_CALENDAR_IS_MISSING(2023, "TaskForSendingCalendarIsMissing", "Communication task ''{0}'' for sending calendar is missing on the device configuration, doesn''t meet the necessary conditions or is inactive on device/device type level", Level.SEVERE),
    CONNECTION_METHOD_DOESNT_MEET_THE_REQUIREMENT(2024, "ConnectionMethodDoesntMeetTheRequirement", "The connection method ''{0}'' set on ''{1}'' doesn't match the one required on the calendar campaign", Level.WARNING),

    CANCELED_BY_USER(3001, "CancelledByUser", "Cancelled by user."),
    RETRIED_BY_USER(3002, "RetriedByUser", "Retried by user.");

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
