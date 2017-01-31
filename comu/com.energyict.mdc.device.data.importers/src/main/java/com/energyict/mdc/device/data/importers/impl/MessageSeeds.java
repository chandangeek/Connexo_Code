/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    DATE_FORMAT_IS_NOT_VALID(1, "DateFormatIsNotValid", "Invalid date format", Level.SEVERE),
    TIME_ZONE_IS_NOT_VALID(2, "TimeZoneIsNotValid", "Invalid time zone", Level.SEVERE),
    NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER(3, "NumberFormatIncompatibleWithDelimiter", "Number format is incompatible with delimiter", Level.SEVERE),

    MISSING_TITLE_ERROR(4, "TitleMissingError", "File format error: wrong number of title columns in the first line. Importer service expects {0} but was {1}.", Level.SEVERE),
    FILE_FORMAT_ERROR(5, "FileFormatError", "File format error: wrong number of columns in the line {0}. Importer service expects {1} but was {2}.", Level.SEVERE),
    LINE_MISSING_VALUE_ERROR(6, "LineMissingValueError", "Format error for line {0}: missing value for column ''{1}''.", Level.SEVERE),
    LINE_FORMAT_ERROR(7, "LineFormatError", "Format error for line {0}: wrong value format for column ''{1}'' (expected format = ''{2}'')", Level.SEVERE),

    NO_DEVICE_TYPE(101, "ImportProcessorNoDeviceType", "Can''t process line {0}: No device type found with name: {1}.", Level.WARNING),
    NO_DEVICE_CONFIGURATION(102, "ImportProcessorNoDeviceConfiguration", "Can''t process line {0}: No device configuration found with name: {1}.", Level.WARNING),
    NO_DEVICE(103, "ImportProcessorNoDevice", "Can''t process line {0}: No device found: {1}.", Level.WARNING),
    NO_MASTER_DEVICE(104, "ImportProcessorNoMasterDevice", "Can''t process line {0}: Master device {1} was not found.", Level.WARNING),
    DEVICE_CAN_NOT_BE_MASTER(105, "ImportProcessorDeviceCanNotBeMaster", "Can''t process line {0}: Master device {0} is not configured to act as master device", Level.WARNING),
    NO_USAGE_POINT(106, "ImportProcessorNoUsagePoint", "Can''t process line {0}: Usage point {1} is not found. " +
            "New usage point can''t be created because of incorrect value of Service category. The list of available Service categories: {2}", Level.WARNING),
    DEVICE_ALREADY_EXISTS(107, "ImportProcessorDeviceAlreadyExists", "Can''t process line {0}: The device (name: {1}) is already in use", Level.WARNING),
    DEVICE_ALREADY_IN_THAT_STATE(108, "ImportProcessorDeviceAlreadyInThatState", "Can''t process line {0}: The device is already in {1} state", Level.WARNING),
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE(109, "ImportProcessorDeviceCanNotBeMovedToState", "Can''t process line {0}: The device can''t be moved from state {2} to {1}", Level.WARNING),
    PRE_TRANSITION_CHECKS_FAILED(110, "ImportProcessorPreTransitionsChecksFailed", "Can''t process line {0}: Pre-transition check(s) failed: {1}", Level.WARNING),
    TRANSITION_ACTION_DATE_IS_INCORRECT(111, "ImportProcessorTransitionActionDateIsIncorrect", "Can''t process line {0}: The transition action date has incorrect value: {1}, {2}", Level.WARNING),
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER(112, "ImportProcessorDeviceCanNotBeMovedToStateByImporter", "Can''t process line {0}: The device can''t be moved from state {2} to {1}. Appropriate initial state(s) is(are) {3}", Level.WARNING),
    INCORRECT_MULTIPLIER_VALUE(113, "ImportProcessorInvalidMultiplierValue", "Can''t process line {0}: The value of the multiplier is not valid {1}", Level.WARNING),
    USELESS_MULTIPLIER_CONFIGURED(114, "ImportProcessorMultiplierConfiguredButNotUsed", "There is a multiplier configured on line {0} with value {1}, but the action 'set multiplier' is not defined on the lifecycle transition", Level.WARNING),

    READING_DATE_BEFORE_METER_ACTIVATION(201, "ReadingDateBeforeMeterActivation", "Note for line {0}: Reading date: {1} is before the first meter activation start date and will not be stored.", Level.WARNING),
    READING_DATE_AFTER_METER_ACTIVATION(202, "ReadingDateAfterMeterActivation", "Note for line {0}: Reading date: {1} is after the last meter activation end date and will not be stored.", Level.WARNING),
    NO_SUCH_READING_TYPE(203, "NoSuchReadingType", "Can''t process line {0}: Reading type {1} doesn''t exist", Level.WARNING),
    NOT_SUPPORTED_READING_TYPE(204, "NotSupportedReadingType", "Can''t process line {0}: Reading type {1} is not supported. Supported reading types: registers and channels with integration time >= day.", Level.WARNING),
    DEVICE_DOES_NOT_SUPPORT_READING_TYPE(205, "DeviceDoesNotSupportReadingType", "Can''t process line {0}: Reading type {1} is not available for device {2}", Level.WARNING),
    READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW(206, "ReadingValueDoesNotMatchRegisterConfigOverflow", "Can''t process line {0}: Reading value for reading type {1} of device {2} doesn''t match with register configuration settings (overflow)", Level.WARNING),
    READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW(207, "ReadingValueDoesNotMatchChannelConfigOverflow", "Can''t process line {0}: Reading value for reading type {1} of device {2} doesn''t match with channel configuration settings (overflow)", Level.WARNING),
    READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG(208, "ReadingValueWasTruncatedToRegisterConfig", "Note for line {0}: Reading value was truncated to {1} according to register configuration.", Level.INFO),
    READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG(209, "ReadingValueWasTruncatedToChannelConfig", "Note for line {0}: Reading value was truncated to {1} according to channel configuration.", Level.INFO),
    READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE(210, "ReadingImportIsNotAllowedForDecommissionedDevices", "Can''t process line {0}: Import service doesn''t have privileges to import readings for device {1} since it is in Decommissioned state.", Level.WARNING),
    READING_IMPORT_NOT_ALLOWED_FOR_IN_STOCK_DEVICE(211, "ReadingImportIsNotAllowedForInStockDevices", "Note for line {0}: Reading can''t be imported for device {1} since this device is in In Stock state and was not installed or commissioned yet.", Level.WARNING),
    READING_DATE_INCORRECT_FOR_DAILY_CHANNEL(212, "ReadingDateIncorrectForDailyChannel", "Can''t process line {0}: Reading date is incorrect for reading type {1}. Time of reading date of daily reading must be midnight in device timezone ({2}).", Level.WARNING),
    READING_DATE_INCORRECT_FOR_MONTHLY_CHANNEL(213, "ReadingDateIncorrectForMonthlyChannel", "Can''t process line {0}: Reading date is incorrect for reading type {1}. Reading date of monthly reading must be the 1st day of the month and time of the day must be midnight in device timezone ({2}).", Level.WARNING),

    NO_CONNECTION_METHOD_ON_DEVICE(301, "NoSuchConnectionMethodOnDevice", "Can''t process line {0}: Connection method {1} is not supported on the device.", Level.WARNING),
    CONNECTION_METHOD_IS_NOT_UNIQUE_IN_FILE(302, "ConnectionMethodIsNotUniqueInFile", "Failure in line {0}: Connection method name is not unique in the file.", Level.SEVERE),
    REQUIRED_CONNECTION_ATTRIBUTES_MISSED(303, "RequiredConnectionAttributesMissed", "Note for line {0}: Next required attribute(s) is(are) missed: {1}. The state of the connection method is \"Incomplete\".", Level.INFO),
    CONNECTION_METHOD_NOT_CREATED(304, "ConnectionMethodNotCreated", "Can''t process line {0}: Connection method {1} could not be created on the device {2}. Reasons: {3}", Level.WARNING),
    CONNECTION_ATTRIBUTE_INVALID_VALUE(305, "ConnectionAttributeInvalidValue", "Can''t process line {0}: Connection attribute value ''{1}'' is invalid for attribute ''{2}''", Level.WARNING),
    UNKNOWN_CONNECTION_ATTRIBUTE(306, "UnknownConnectionAttribute", "Note for file: Connection method {0} doesn''t have next connection attribute(s): {1}", Level.INFO),

    NO_SECURITY_SETTINGS_ON_DEVICE(401, "NoSuchSecuritySettingsOnDevice", "Can''t process line {0}: Security settings with name {1} is not available on the device.", Level.WARNING),
    SECURITY_ATTRIBUTES_NOT_SET(402, "SecurityAttributesNotCreated", "Can''t process line {0}: Security attributes are not set on the device {1}.", Level.WARNING),
    SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE(403, "SecuritySettingsNameIsNotUniqueInFile", "Failure in line {0}: Security settings name is not unique in the file.", Level.WARNING),
    REQUIRED_SECURITY_ATTRIBUTES_MISSED(404, "RequiredSecurityAttributesMissed", "Note for line {0}: Next required attribute(s) is(are) missed: {1}. The state of the security settings is \"Incomplete\".", Level.INFO),
    SECURITY_ATTRIBUTE_INVALID_VALUE(405, "SecurityAttributeInvalidValue", "Can''t process line {0}: Security attribute value ''{1}'' is invalid for attribute ''{2}''", Level.WARNING),
    INCORRECT_LOCATION_FORMAT(406, "IncorrectLocationFormat", "Incorrect location format. Expected : {0}", Level.SEVERE),
    LINE_MISSING_LOCATION_VALUE(407, "LineMissingLocationValue", "Can''t process line {0}: missing value for field ''{1}''.", Level.SEVERE),

    USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE(501, "UsagePointAlreadyLinkedToAnotherDeviceX", "Can''t process line {0}: Usage point {1} is already linked to device {2} starting from {3}", Level.WARNING),
    USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE_UNTIL(502, "UsagePointAlreadyLinkedToAnotherDeviceXUntil", "Can''t process line {0}: Usage point {1} is already linked to device {2} from {3} until {4}", Level.WARNING),
    UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT(503, "DeviceDoesNotProvideRequiredReadingTypes", "Can''t process line {0}: Device {1} doesn''t have the following reading types that are specified in the metrology configurations of selected usage point {2}: {3}", Level.WARNING),
    PROCESS_SQL_EXCEPTION(504, "ProcessSqlException", "Can''t process line {0}.  There was a problem accessing the database", Level.SEVERE),
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
        return DeviceDataImporterMessageHandler.COMPONENT;
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
}
