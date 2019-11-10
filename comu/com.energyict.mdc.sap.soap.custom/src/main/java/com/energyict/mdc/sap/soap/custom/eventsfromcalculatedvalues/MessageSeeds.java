/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    THIS_FIELD_IS_REQUIRED(1, Keys.THIS_FIELD_IS_REQUIRED, "This field is required."),
    VALUE_MUST_BE_POSITIVE(2, Keys.VALUE_MUST_BE_POSITIVE, "Value must be positive."),
    PERCENTAGE_VALUE_NOT_VALID(3, Keys.PERCENTAGE_VALUE_NOT_VALID, "Percentage value must be less than or equal to 100."),

    PROPERTY_IS_NOT_SET(10, "PropertyNotSet", "Property ''{0}'' isn''t set."),
    PROPERTY_VALUE_CANNOT_BE_EMPTY(11, "PropertyValueCannotBeEmpty", "Property value of the device type or reading type can''t be empty for the config parameter ''{0}''."),
    PROPERTY_VALUE_FORMAT_IS_INVALID(12, "PropertyValueFormatIsInvalid", "Invalid format of the config parameter ''{0}''."),

    POWER_FACTOR_MISSING_READING(100, "PowerFactorMissingReading", "Insufficient data for power factor calculation: no reading available on device {0} on reading type {1} for interval {2}."),
    POWER_FACTOR_VALUES_ARE_NULL(101, "PowerFactorValuesAreNull", "Can''t calculate power factor on device {0} on reading types {1} for interval {2} due to empty values."),
    POWER_FACTOR_INVALID_READING_TYPE(102, "PowerFactorInvalidReadingType", "Register reading types aren''t supported by power factor calculation on device type {0}."),
    POWER_FACTOR_READING_TYPES_MUST_HAVE_THE_SAME_INTERVAL(103, "PowerFactorReadingTypesMustHaveTheSameInterval", "Power factor reading types {0} must have the same time attribute."),

    READING_TYPE_IS_NOT_FOUND(200, "ReadingTypeIsNotFound", "Reading type {0} isn''t found."),
    UNEXPECTED_UNIT_FOR_READING_TYPE(201, "UnexpectedUnitOnReadingType", "Unexpected unit for reading type {0}.");

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
        return DataExportService.COMPONENTNAME;
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

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public static final class Keys {
        public static final String THIS_FIELD_IS_REQUIRED = "ThisFieldIsRequired";
        public static final String VALUE_MUST_BE_POSITIVE = "ValueMustBePositive";
        public static final String PERCENTAGE_VALUE_NOT_VALID = "PercentageValueNotValid";
    }
}
