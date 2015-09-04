package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    NO_DEVICE(103, "ImportProcessorNoDevice", "Can''t process line {0}: No device found with MRID: {1}.", Level.WARNING),
    NO_MASTER_DEVICE(104, "ImportProcessorNoMasterDevice", "Can''t process line {0}: Master device with MRID: {1} was not found.", Level.WARNING),
    DEVICE_CAN_NOT_BE_MASTER(105, "ImportProcessorDeviceCanNotBeMaster", "Can''t process line {0}: Master device with MRID: {0} is not configured to act as master device", Level.WARNING),
    NO_USAGE_POINT(106, "ImportProcessorNoUsagePoint", "Can''t process line {0}: Usage point with MRID: {1} is not found. " +
            "New usage point can't be created because of incorrect value of Service category. The list of available Service categories: {2}", Level.WARNING),
    DEVICE_ALREADY_EXISTS(107, "ImportProcessorDeviceAlreadyExists", "Can''t process line {0}: The device (MRID: {1}) is already in use", Level.WARNING),
    DEVICE_ALREADY_IN_THAT_STATE(108, "ImportProcessorDeviceAlreadyInThatState", "Can''t process line {0}: The device is already in {1} state", Level.WARNING),
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE(109, "ImportProcessorDeviceCanNotBeMovedToState", "Can''t process line {0}: The device can''t be moved to {1} from {2} state", Level.WARNING),
    PRE_TRANSITION_CHECKS_FAILED(110, "ImportProcessorPreTransitionsChecksFailed", "Can''t process line {0}: Pre-transition check(s) failed: {1}", Level.WARNING),
    TRANSITION_ACTION_DATE_IS_INCORRECT(111, "ImportProcessorTransitionActionDateIsIncorrect", "Can''t process line {0}: The transition action date has incorrect value: {1}, {2}", Level.WARNING),
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER(112, "ImportProcessorDeviceCanNotBeMovedToStateByImporter", "Can''t process line {0}: The device can''t be moved to {1} from {2} state by this importer. Appropriate initial state(s) is(are) {3}", Level.WARNING),

    READING_DATE_BEFORE_METER_ACTIVATION(201, "ReadingDateBeforeMeterActivation", "Note for line {0}: Reading date: {1} is before the first meter activation start date and will not be stored.", Level.WARNING),
    READING_DATE_AFTER_METER_ACTIVATION(202, "ReadingDateAfterMeterActivation", "Note for line {0}: Reading date: {1} is after the last meter activation end date and will not be stored.", Level.WARNING),
    NO_SUCH_READING_TYPE(203, "NoSuchReadingType", "Can''t process line {0}: Reading type {1} doesn''t exist", Level.WARNING),
    NOT_SUPPORTED_READING_TYPE(204, "NotSupportedReadingType", "Can''t process line {0}: Reading type {1} is not supported. Supported reading types: registers and channels with integration time >= day.", Level.WARNING),
    DEVICE_DOES_NOT_SUPPORT_READING_TYPE(205, "DeviceDoesNotSupportReadingType", "Can''t process line {0}: Reading type {1} is not available for the device with MRID: {2}", Level.WARNING),
    READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW(206, "ReadingValueDoesNotMatchRegisterConfigOverflow", "Can''t process line {0}: Reading value for reading type {1} of device with MRID: {2} doesn''t match with register configuration settings (overflow)", Level.WARNING),
    READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW(207, "ReadingValueDoesNotMatchChannelConfigOverflow", "Can''t process line {0}: Reading value for reading type {1} of device with MRID: {2} doesn''t match with channel configuration settings (overflow)", Level.WARNING),
    READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG(208, "ReadingValueWasTruncatedToRegisterConfig", "Note for line {0}: Reading value was truncated to {1} accordingly to register configuration.", Level.INFO),
    READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG(209, "ReadingValueWasTruncatedToChannelConfig", "Note for line {0}: Reading value was truncated to {1} accordingly to channel configuration.", Level.INFO),
    READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE(210, "ReadingImportIsNotAllowedForDecommissionedDevices", "Can''t process line {0}: Readings import is not allowed for device {1} since it is decommissioned.", Level.WARNING),

    NO_CONNECTION_METHOD_ON_DEVICE(301, "NoSuchConnectionMethodOnDevice", "Can''t process line {0}: Connection method {1} is not supported on the device.", Level.WARNING),
    CONNECTION_METHOD_IS_NOT_UNIQUE_IN_FILE(302, "ConnectionMethodIsNotUniqueInFile", "Failure in line {0}: Connection method name is not unique in the file.", Level.SEVERE),
    REQUIRED_CONNECTION_ATTRIBUTES_MISSED(303, "RequiredConnectionAttributesMissed", "Note for line {0}: Next required attribute(s) is(are) missed: {1}. The state of the connection method is \"Incomplete\".", Level.INFO),
    CONNECTION_METHOD_NOT_CREATED(304, "ConnectionMethodNotCreated", "Can''t process line {0}: Connection method {1} could not be created on the device {2}. Reasons: {3}", Level.WARNING),
    CONNECTION_ATTRIBUTE_INVALID_VALUE(305, "ConnectionAttributeInvalidValue", "Can''t process line {0}: Connection attribute value ''{1}'' is invalid for attribute ''{2}''", Level.WARNING),

    NO_SECURITY_SETTINGS_ON_DEVICE(401, "NoSuchSecuritySettingsOnDevice", "Can''t process line {0}: Security settings with name {1} is not available on the device.", Level.WARNING),
    SECURITY_ATTRIBUTES_NOT_SET(402, "SecurityAttributesNotCreated", "Can''t process line {0}: Security attributes are not set on the device {1}.", Level.WARNING),
    SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE(403, "SecuritySettingsNameIsNotUniqueInFile", "Failure in line {0}: Security settings name is not unique in the file.", Level.WARNING),
    REQUIRED_SECURITY_ATTRIBUTES_MISSED(404, "RequiredSecurityAttributesMissed", "Note for line {0}: Next required attribute(s) is(are) missed: {1}. The state of the security settings is \"Incomplete\".", Level.INFO),
    SECURITY_ATTRIBUTE_INVALID_VALUE(405, "SecurityAttributeInvalidValue", "Can''t process line {0}: Security attribute value ''{1}'' is invalid for attribute ''{2}''", Level.WARNING),
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

    public String getTranslated(Thesaurus thesaurus, Object... args) {
        String translated = thesaurus.getString(this.getKey(), this.getDefaultFormat());
        return MessageFormat.format(translated, args);
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }
}