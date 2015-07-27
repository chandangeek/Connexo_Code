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
    DEVICE_CAN_NOT_BE_MOVED_TO_STATE(16, "ImportProcessorDeviceCanNotBeMovedToState", "Error in line {0}: The device can't be moved to {1} from {2} state", Level.WARNING),
    PRE_TRANSITION_CHECKS_FAILED(17, "ImportProcessorPreTransitionsChecksFailed", "Error in line {0}: Pre-transition check(s) failed: {1}", Level.WARNING),
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

    public String getTranslated(Thesaurus thesaurus, Object... args){
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