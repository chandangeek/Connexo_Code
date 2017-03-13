/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    DATE_FORMAT_IS_NOT_VALID(1, "DateFormatIsNotValid", "Invalid date format"),
    TIME_ZONE_IS_NOT_VALID(2, "TimeZoneIsNotValid", "Invalid time zone"),
    NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER(3, "NumberFormatIncompatibleWithDelimiter", "Number format is incompatible with delimiter"),

    MISSING_TITLE_ERROR(4, "TitleMissingError", "File format error: wrong number of title columns in the first line. Importer service expects {0} but was {1}."),
    FILE_FORMAT_ERROR(5, "FileFormatError", "File format error: wrong number of columns in the line {0}. Importer service expects {1} but was {2}."),
    LINE_MISSING_VALUE_ERROR(6, "LineMissingValueError", "Format error for line {0}: missing mandatory value for column {1}."),
    LINE_FORMAT_ERROR(7, "LineFormatError", "Format error for line {0}: wrong value format for column {1} (expected format = ''{2}'')"),
    UPDATE_NOT_ALLOWED(8, Constants.UPDATE_NOT_ALLOWED, "Can''t process line {0}: Update not allowed"),

    IMPORT_USAGEPOINT_SUCCEEDED(1010, Constants.IMPORT_SUCCEEDED, " {0} usage points successfully imported without any errors", Level.INFO),
    IMPORT_USAGEPOINT_SUCCEEDED_WITH_FAILURES(1011, Constants.IMPORT_SUCCEEDED_WITH_FAILURES, " {0} usage points successfully imported, {1} usage points failed", Level.WARNING),
    IMPORT_USAGEPOINT_EXCEPTION(1012, Constants.IMPORT_USAGEPOINT_EXCEPTION, " Import failed. Please check file content format"),
    IMPORT_USAGEPOINT_INVALIDDATA(1013, Constants.IMPORT_USAGEPOINT_INVALID_DATA, " Can''t process line {0}: Invalid data"),
    IMPORT_USAGEPOINT_SERVICEKIND_INVALID(1014, Constants.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, " Can''t process line {0}: Invalid service kind"),
    IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND(1015, Constants.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, " Can''t process line {0}: No service kind found with name: {1}"),
    IMPORT_USAGEPOINT_SERVICELOCATION_INVALID(1016, Constants.IMPORT_USAGEPOINT_INVALID_SERVICELOCATION, " Invalid service location in line {0}. Attribute skipped.", Level.WARNING),
    IMPORT_USAGEPOINT_IDENTIFIER_INVALID(1017, Constants.IMPORT_USAGEPOINT_INVALID_IDENTIFIER, " Can''t process line {0}: Invalid identifier (MRID or name)"),
    IMPORT_USAGEPOINT_PARSER_INVALID(1018, Constants.IMPORT_PARSER_INVALID, "Can''t process line {0}: Parser not found for {0}"),
    IMPORT_USAGEPOINT_NOT_FOUND(1019, Constants.IMPORT_USAGEPOINT_NOT_FOUND, "Can''t process line {0}: No usage point found: {1}"),
    IMPORT_USAGEPOINT_CONSTRAINT_VOLATION(1020, Constants.IMPORT_USAGEPOINT_CONSTRAINT_VOLATION, " Can''t process line {0}: {1} - {2}"),
    IMPORT_QUANITITY_OUT_OF_BOUNDS(1021, Constants.IMPORT_QUANITITY_OUT_OF_BOUNDS, " Can''t process line {0}: Invalid data. Multiplier out of bounds"),
    IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID(1022, Constants.IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID, " Can''t process line {0}: No service category found with name: {1}"),
    IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE(1023, Constants.IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE, " Can''t process line {0}: It''s not possible to change service category on usage point"),
    IMPORT_VERSIONED_VALUES_NOT_FOUND(2001, Constants.IMPORT_VERSIONED_VALUES_NOT_FOUND, "Can''t process line {0}: No such active versions for {1}"),
    LINE_MISSING_LOCATION_VALUE(2002, Constants.LINE_MISSING_LOCATION_VALUE, "Format error for line {0}: missing mandatory value for field {1}."),
    BAD_METROLOGY_CONFIGURATION(2003, Constants.BAD_METROLOGY_CONFIGURATION, "Can''t process line {0}: No such active metrology configuration"),
    SERVICE_CATEGORIES_DO_NOT_MATCH(2004, Constants.SERVICE_CATEGORIES_DO_NOT_MATCH, "Can''t process line {0}: The service category of usage point doesn't match to service category of metrology configuration"),
    EMPTY_METROLOGY_CONFIGURATION_TIME(2005, Constants.EMPTY_METROLOGY_CONFIGURATION_TIME, "Can''t process line {0}: Start date of metrology configuration is not specified"),
    NO_SUCH_MANDATORY_CPS_VALUE(2005, Constants.NO_SUCH_MANDATORY_CPS_VALUE, "Can''t process line {0}: Missing mandatory value {1} for custom property set {2}"),
    NO_SUCH_METER_WITH_NAME(2006, Constants.NO_SUCH_METER, "No meter with name {0} was found", Level.WARNING),
    NO_SUCH_METER_ROLE_WITH_KEY(2007, Constants.NO_SUCH_METER_ROLE, "No meter role with key {0} was found", Level.WARNING),
    ACTIVATION_DATE_OF_METER_ROLE_IS_BEFORE_UP_CREATION(2008, Constants.ACTIVATION_DATE_OF_METER_ROLE_IS_BEFORE_UP_CREATION, "Activation date of meter {0} must be grater or equal than 'Created' date of usage point", Level.WARNING),
    SOME_REQUIRED_FIELDS_ARE_EMPTY(2009, Constants.SOME_REQUIRED_FIELDS_ARE_EMPTY, "Meter and activation date must be specified together for usage point", Level.WARNING),
    NO_SUCH_TRANSITION_FOUND(2010, Constants.NO_SUCH_TRANSITION, "Transition {0} cannot be performed on usage point", Level.WARNING),
    TRANSITION_DATE_IS_NOT_SPECIFIED(2011, Constants.TRANSITION_DATE_IS_NOT_SPECIFIED, "Transition date isn't specified for usage point", Level.WARNING),
    ACTIVATION_DATE_OF_TRANSITION_IS_BEFORE_UP_CREATION(2012, Constants.ACTIVATION_DATE_OF_TRANSITION_IS_BEFORE_UP_CREATION, "Transition date must be greater or equal to 'Created' date of usage point", Level.WARNING),
    PRE_TRANSITION_CHECK_FAILED(2013, Constants.PRE_TRANSITION_CHECK_FAILED, "Pre-transition check failed {0}", Level.WARNING),
    CALENDAR_DOES_NOT_EXIST(20014, Constants.CALENDAR_DOES_NOT_EXIST, "Can''t process line {0}: No such {1} calendar");

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
        return UsagePointFileImporterMessageHandler.COMPONENT_NAME;
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
        logger.log(getLevel(), formatMessage(thesaurus, args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        logger.log(getLevel(), formatMessage(thesaurus, args), t);
    }

    public String formatMessage(Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        return format.format(args);
    }

    public enum Constants {
        ;
        public static final String IMPORT_SUCCEEDED = "up.import.succeeded";
        public static final String IMPORT_SUCCEEDED_WITH_FAILURES = "up.import.succeeded.with.failures";
        public static final String IMPORT_USAGEPOINT_EXCEPTION = "up.import.exception";
        public static final String IMPORT_USAGEPOINT_INVALID_IDENTIFIER = "up.invalid.id";
        public static final String IMPORT_USAGEPOINT_INVALID_DATA = "up.invalid.date";
        public static final String IMPORT_USAGEPOINT_SERVICEKIND_INVALID = "up.no.service.kind";
        public static final String IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND = "up.invalid.service.kind";
        public static final String IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID = "up.invalid.service.category";
        public static final String IMPORT_USAGEPOINT_INVALID_SERVICELOCATION = "up.invalid.service.location";
        public static final String IMPORT_PARSER_INVALID = "up.invalid.parser";
        public static final String IMPORT_VERSIONED_VALUES_NOT_FOUND = "up.versioned.values.not.found";
        public static final String UPDATE_NOT_ALLOWED = "update.not.allowed";
        public static final String IMPORT_USAGEPOINT_NOT_FOUND = "up.not.found";
        public static final String IMPORT_USAGEPOINT_CONSTRAINT_VOLATION = "up.constraint.violation";
        public static final String IMPORT_QUANITITY_OUT_OF_BOUNDS = "quantity.out.of.bounds";
        public static final String IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE = "up.service.category.cannot.change";
        public static final String LINE_MISSING_LOCATION_VALUE = "line.missing.location.value";
        public static final String BAD_METROLOGY_CONFIGURATION = "bad.metrology.configuration";
        public static final String CALENDAR_DOES_NOT_EXIST = "calendar.does.not.exist";
        public static final String SERVICE_CATEGORIES_DO_NOT_MATCH = "service.categories.do.not.match";
        public static final String EMPTY_METROLOGY_CONFIGURATION_TIME = "empty.metrology.configuration.time";
        public static final String NO_SUCH_MANDATORY_CPS_VALUE = "no.such.mandatory.cps.value";
        public static final String NO_SUCH_METER = "no.such.meter";
        public static final String NO_SUCH_METER_ROLE = "no.such.meter.role";
        public static final String ACTIVATION_DATE_OF_METER_ROLE_IS_BEFORE_UP_CREATION = "activation.date.of.meter.role.is.before.up.creation";
        public static final String SOME_REQUIRED_FIELDS_ARE_EMPTY = "some.fields.are.empty";
        public static final String NO_SUCH_TRANSITION = "no.such.transition";
        public static final String TRANSITION_DATE_IS_NOT_SPECIFIED = "transition.date.is.not.specified";
        public static final String ACTIVATION_DATE_OF_TRANSITION_IS_BEFORE_UP_CREATION = "activation.date.of.transition.is.before.up.creation";
        public static final String PRE_TRANSITION_CHECK_FAILED = "pre.transition.check.failed";
    }
}