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
    DATE_FORMAT_IS_NOT_VALID(1, "DateFormatIsNotValid", "Invalid date format", Level.SEVERE),
    TIME_ZONE_IS_NOT_VALID(2, "TimeZoneIsNotValid", "Invalid time zone", Level.SEVERE),
    NUMBER_FORMAT_IS_INCOMPATIBLE_WITH_DELIMITER(3, "NumberFormatIncompatibleWithDelimiter", "Number format is incompatible with delimiter", Level.SEVERE),

    MISSING_TITLE_ERROR(4, "TitleMissingError", "File format error: wrong number of title columns in the first line. Importer service expects {0} but was {1}.", Level.SEVERE),
    FILE_FORMAT_ERROR(5, "FileFormatError", "File format error: wrong number of columns in the line {0}. Importer service expects {1} but was {2}.", Level.SEVERE),
    LINE_MISSING_VALUE_ERROR(6, "LineMissingValueError", "Format error for line {0}: missing mandatory value for column {1}.", Level.SEVERE),
    LINE_FORMAT_ERROR(7, "LineFormatError", "Format error for line {0}: wrong value format for column {1} (expected format = ''{2}'')", Level.SEVERE),
    UPDATE_NOT_ALLOWED(8, Constants.UPDATE_NOT_ALLOWED, "Can''t process line {0}: Update not allowed", Level.SEVERE),

    IMPORT_USAGEPOINT_SUCCEEDED(1010, Constants.IMPORT_SUCCEEDED, " {0} usage points successfully imported without any errors", Level.INFO),
    IMPORT_USAGEPOINT_SUCCEEDED_WITH_FAILURES(1011, Constants.IMPORT_SUCCEEDED_WITH_FAILURES, " {0} usage points successfully imported, {1} usage points failed", Level.WARNING),
    IMPORT_USAGEPOINT_EXCEPTION(1012, Constants.IMPORT_USAGEPOINT_EXCEPTION, " Import failed. Please check file content format", Level.SEVERE),
    IMPORT_USAGEPOINT_INVALIDDATA(1013, Constants.IMPORT_USAGEPOINT_INVALID_DATA, " Can''t process line {0}: Invalid data", Level.SEVERE),
    IMPORT_USAGEPOINT_SERVICEKIND_INVALID(1014, Constants.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, " Can''t process line {0}: Invalid service kind", Level.SEVERE),
    IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND(1015, Constants.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, " Can''t process line {0}: No service kind found with name: {1}", Level.SEVERE),
    IMPORT_USAGEPOINT_SERVICELOCATION_INVALID(1016, Constants.IMPORT_USAGEPOINT_INVALID_SERVICELOCATION, " Invalid service location in line {0}. Attribute skipped.", Level.WARNING),
    IMPORT_USAGEPOINT_IDENTIFIER_INVALID(1017, Constants.IMPORT_USAGEPOINT_INVALID_IDENTIFIER, " Can''t process line {0}: Invalid identifier (MRID or name)", Level.SEVERE),
    IMPORT_USAGEPOINT_PARSER_INVALID(1018, Constants.IMPORT_PARSER_INVALID, "Can''t process line {0}: Parser not found for {0}", Level.SEVERE),
    IMPORT_USAGEPOINT_NOT_FOUND(1019, Constants.IMPORT_USAGEPOINT_NOT_FOUND, "Can''t process line {0}: No usage point found: {1}", Level.SEVERE),
    IMPORT_USAGEPOINT_CONSTRAINT_VOLATION(1020, Constants.IMPORT_USAGEPOINT_CONSTRAINT_VOLATION, " Can''t process line {0}: {1} - {2}", Level.SEVERE),
    IMPORT_QUANITITY_OUT_OF_BOUNDS(1021, Constants.IMPORT_QUANITITY_OUT_OF_BOUNDS, " Can''t process line {0}: Invalid data. Multiplier out of bounds", Level.SEVERE),
    IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID(1022, Constants.IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID, " Can''t process line {0}: No service category found with name: {1}", Level.SEVERE),
    IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE(1023, Constants.IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE, " Can''t process line {0}: It''s not possible to change service category on usage point", Level.SEVERE),
    IMPORT_VERSIONED_VALUES_NOT_FOUND(2001, Constants.IMPORT_VERSIONED_VALUES_NOT_FOUND, "Can''t process line {0}: No such active versions for {1}", Level.SEVERE),
    LINE_MISSING_LOCATION_VALUE(2002, Constants.LINE_MISSING_LOCATION_VALUE, "Format error for line {0}: missing mandatory value for field {1}.", Level.SEVERE),
    BAD_METROLOGY_CONFIGURATION(2003, Constants.BAD_METROLOGY_CONFIGURATION, "Can''t process line {0}: No such active metrology configuration", Level.SEVERE),
    SERVICE_CATEGORIES_DO_NOT_MATCH(2004, Constants.SERVICE_CATEGORIES_DO_NOT_MATCH, "Can''t process line {0}: The service category of usage point doesn't match to service category of metrology configuration", Level.SEVERE),
    EMPTY_METROLOGY_CONFIGURATION_TIME(2005, Constants.EMPTY_METROLOGY_CONFIGURATION_TIME, "Can''t process line {0}: Start date of metrology configuration is not specified", Level.SEVERE),
    NO_SUCH_MANDATORY_CPS_VALUE(2005, Constants.NO_SUCH_MANDATORY_CPS_VALUE, "Can''t process line {0}: Missing mandatory value {1} for custom property set {2}", Level.SEVERE),
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
        public static final String SERVICE_CATEGORIES_DO_NOT_MATCH = "service.categories.do.not.match";
        public static final String EMPTY_METROLOGY_CONFIGURATION_TIME = "empty.metrology.configuration.time";
        public static final String NO_SUCH_MANDATORY_CPS_VALUE = "no.such.mandatory.cps.value";
    }
}