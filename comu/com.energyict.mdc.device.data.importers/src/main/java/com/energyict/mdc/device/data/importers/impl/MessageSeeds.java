package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    DATE_FORMAT_IS_NOT_VALID(1, "DateFormatIsNotValid", "Invalid date format", Level.SEVERE),
    TIME_ZONE_IS_NOT_VALID(2, "TimeZoneIsNotValid", "Invalid time zone", Level.SEVERE),
    NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER(3, "NumberFormatIncompatibleWithDelimiter", "Number format is incompatible with delimiter", Level.SEVERE),

    MISSING_TITLE_ERROR(4, "TitleMissingError", "File format error: wrong number of title columns in the first line. Importer service expects {0} but was {1}.", Level.SEVERE),
    FILE_FORMAT_ERROR(5, "FileFormatError", "File format error: wrong number of columns in the line {0}. Importer service expects {1} but was {2}.", Level.SEVERE),
    LINE_MISSING_VALUE_ERROR(6, "LineMissingValueError", "Format error for line {0}: missing value for column ''{1}''.", Level.SEVERE),
    LINE_FORMAT_ERROR(7, "LineFormatError", "Format error for line {0}: wrong value format for column ''{1}'' (expected format = ''{2}'')", Level.SEVERE),

    NO_DEVICE_TYPE(8, "ImportProcessorNoDeviceType", "Error in line {0}: No device type found with name: {1}.", Level.WARNING),
    NO_DEVICE_CONFIGURATION(9, "ImportProcessorNoDeviceConfiguration", "Error in line {0}: No device configuration found with name: {1}.", Level.WARNING),
    NO_DEVICE(10, "ImportProcessorNoDevice", "Error in line {0}: No device found with MRID: {1}.", Level.WARNING),
    NO_MASTER_DEVICE(11, "ImportProcessorNoMasterDevice", "Error in line {0}: Master device with MRID: {1} was not found.", Level.WARNING),
    DEVICE_CAN_NOT_BE_MASTER(12, "ImportProcessorDeviceCanNotBeMaster", "Error in line {0}: Master device with MRID: {0} is not configured to act as master device", Level.WARNING),
    NO_USAGE_POINT(13, "ImportProcessorNoUsagePoint", "Error in line {0}: Usage point with MRID: {1} is not found. " +
            "New usage point can't be created because of incorrect value of Service category. The list of available Service categories: {2}", Level.WARNING),

    DEVICE_ALREADY_EXISTS(14, "ImportProcessorDeviceAlreadyExists", "Error in line {0}: The device (MRID: {1}) is already in use", Level.WARNING),
    DEVICE_ALREADY_IN_THAT_STATE(15, "ImportProcessorDeviceAlreadyInThatState", "Error in line {0}: The device is already in {1} state", Level.WARNING),
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE(16, "ImportProcessorDeviceCanNotBeMovedToState", "Error in line {0}: The device can''t be moved to {1} from {2} state", Level.WARNING),
    PRE_TRANSITION_CHECKS_FAILED(17, "ImportProcessorPreTransitionsChecksFailed", "Error in line {0}: Pre-transition check(s) failed: {1}", Level.WARNING),
    TRANSITION_ACTION_DATE_IS_INCORRECT(18, "ImportProcessorTransitionActionDateIsIncorrect", "Error in line {0}: The transition action date has incorrect value: {1}, {2}", Level.WARNING),
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE_BY_IMPORTER(19, "ImportProcessorDeviceCanNotBeMovedToStateByImporter", "Error in line {0}: The device can''t be moved to {1} from {2} state by this importer. Appropriate initial state(s) is(are) {3}", Level.WARNING),

    READING_DATE_BEFORE_METER_ACTIVATION(101, "ReadingDateBeforeMeterActivation", "Warning for line {0}: Reading date: {1} is before the first meter activation start date and will not be stored.", Level.WARNING),
    READING_DATE_AFTER_METER_ACTIVATION(102, "ReadingDateAfterMeterActivation", "Warning for line {0}: Reading date: {1} is after the last meter activation end date and will not be stored.", Level.WARNING),
    NO_SUCH_READING_TYPE(103, "NoSuchReadingType", "Error in line {0}: Reading type {1} doesn''t exist", Level.WARNING),
    NOT_SUPPORTED_READING_TYPE(104, "NotSupportedReadingType", "Error in line {0}: Reading type {1} is not supported. Supported reading types: registers and channels with integration time >= day.", Level.WARNING),
    DEVICE_DOES_NOT_SUPPORT_READING_TYPE(105, "DeviceDoesNotSupportReadingType", "Error in line {0}: Reading type {1} is not available for the device with MRID: {2}", Level.WARNING),
    READING_VALUE_DOES_NOT_MATCH_REGISTER_CONFIG_OVERFLOW(106, "ReadingValueDoesNotMatchRegisterConfigOverflow", "Error in line {0}: Reading value for reading type {1} of device with MRID: {2} doesn''t match with register configuration settings (overflow)", Level.WARNING),
    READING_VALUE_DOES_NOT_MATCH_CHANNEL_CONFIG_OVERFLOW(107, "ReadingValueDoesNotMatchChannelConfigOverflow", "Error in line {0}: Reading value for reading type {1} of device with MRID: {2} doesn''t match with channel configuration settings (overflow)", Level.WARNING),
    READING_VALUE_WAS_TRUNCATED_TO_REGISTER_CONFIG(108, "ReadingValueWasTruncatedToRegisterConfig", "Warning for line {0}: Reading value was truncated to {1} accordingly to register configuration.", Level.INFO),
    READING_VALUE_WAS_TRUNCATED_TO_CHANNEL_CONFIG(109, "ReadingValueWasTruncatedToChannelConfig", "Warning for line {0}: Reading value was truncated to {1} accordingly to channel configuration.", Level.INFO),
    READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE(110, "ReadingImportIsNotAllowedForDecommissionedDevices", "Error in line {0}: Readings import is not allowed for device {1} since it is decommissioned.", Level.WARNING),

    NO_CONNECTION_METHOD_ON_DEVICE(201, "NoSuchConnectionMethodOnDevice", "Error in line {0}: Connection method {1} is not supported on the device.", Level.WARNING),
    CONNECTION_ATTRIBUTES_NOT_CREATED(202, "ConnectionAttributesNotCreated", "Error in line {0}: Connection attributes are not created on the device {1}.", Level.WARNING),
    CONNECTION_METHOD_IS_NOT_UNIQUE_IN_FILE(203, "ConnectionMethodIsNotUniqueInFile", "Failure in line {0}: Connection method name is not unique in the file.", Level.SEVERE),
    REQUIRED_CONNECTION_ATTRIBUTES_MISSED(204, "RequiredConnectionAttributesMissed", "Warning for line {0}: Next required attribute(s) is(are) missed: {1}. The state of the connection method is \"Incomplete\".", Level.INFO),

    NO_SECURITY_SETTINGS_ON_DEVICE(301, "NoSuchSecuritySettingsOnDevice", "Error in line {0}: Security settings with name {1} is not available on the device.", Level.WARNING),
    SECURITY_ATTRIBUTES_NOT_SET(302, "SecurityAttributesNotCreated", "Error in line {0}: Security attributes are not set on the device {1}.", Level.WARNING),
    SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE(303, "SecuritySettingsNameIsNotUniqueInFile", "Failure in line {0}: Security settings name is not unique in the file.", Level.WARNING),
    REQUIRED_SECURITY_ATTRIBUTES_MISSED(304, "RequiredSecurityAttributesMissed", "Warning for line {0}: Next required attribute(s) is(are) missed: {1}. The state of the security settings is \"Incomplete\".", Level.INFO),
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