/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igh on 18/04/2016.
 */
public enum MessageSeeds implements MessageSeed {

    REQUIRED(1001, Constants.REQUIRED, "This field is required"),
    DAYTYPES_REQUIRED(1002, Constants.DAYTYPES_REQUIRED, "At least one daytype is required"),
    PERIODS_REQUIRED(1003, Constants.PERIODS_REQUIRED, "At least one period is required"),

    CALENDAR_CREATED(1004, Constants.CALENDAR_CREATED, "Calendar has been created.", Level.INFO),
    CALENDAR_UPDATED(1005, Constants.CALENDAR_UPDATED, "Calendar has been updated.", Level.INFO),

    SCHEMA_FAILED(2001, Constants.SCHEMA_FAILED, "XSD schema for calendar import could not be read."),
    JAXB_FAILED(2002, Constants.JAXB_FAILED, "JAXB error occurred."),
    PROPERTY_NOT_FOUND_ON_EVENT(2003, Constants.PROPERTY_NOT_FOUND_ON_EVENT, "Missing property \"{0}\" on event."),
    INVALID_EVENT_CODE(2003, Constants.INVALID_EVENT_CODE, "Event code \"{0}\" should be numeric."),
    MISSING_CALENDAR_NAME(2004, Constants.MISSING_CALENDAR_NAME, "Calendar name is missing."),
    MISSING_TIMEZONE(2005, Constants.MISSING_TIMEZONE, "Calendar timezone is missing."),
    MISSING_STARTYEAR(2006, Constants.MISSING_STARTYEAR, "Calendar start year is missing."),
    STARTYEAR_CANNOT_BE_ZERO(2007, Constants.STARTYEAR_CANNOT_BE_ZERO, "Calendar start year cannot be zero."),
    NO_DAYTYPE_DEFINED_WITH_ID(2008, Constants.NO_DAYTYPE_DEFINED_WITH_ID, "There is no daytype defined with id \"{0}\"."),
    NO_PERIOD_DEFINED_WITH_ID(2009, Constants.NO_PERIOD_DEFINED_WITH_ID, "There is no period defined with id \"{0}\"."),
    INVALID_EVENT_ID(2010, Constants.INVALID_EVENT_ID, "Event id \"{0}\" should be numeric."),
    NO_EVENT_DEFINED_WITH_ID(2011, Constants.NO_EVENT_DEFINED_WITH_ID, "There is no event defined with id \"{0}\"."),
    NO_TIMEZONE_FOUND_WITH_ID(2013, Constants.NO_TIMEZONE_FOUND_WITH_ID, "Timzone \"{0}\" does not exist."),
    YEAR_NOT_ALLOWED_FOR_RECURRING_TRANSITIONS(2014, Constants.YEAR_NOT_ALLOWED_FOR_RECURRING_TRANSITIONS, "\"year\" is not allowed for recurring transitions."),
    YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS(2015, Constants.YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS, "\"year\" is required for non recurring transitions."),
    VALIDATION_OF_FILE_SUCCEEDED(2016, Constants.VALIDATION_OF_FILE_SUCCEEDED, "Validation succeeded.", Level.INFO),
    VALIDATION_OF_FILE_FAILED(2017, Constants.VALIDATION_OF_FILE_FAILED, "Validation failed, please check your file for invalid content."),
    VALIDATION_OF_FILE_FAILED_WITH_DETAIL(2017, Constants.VALIDATION_OF_FILE_FAILED_WITH_DETAIL, "Validation failed: \"{0}\""),
    DUPLICATE_CALENDAR_MRID(2018, Constants.DUPLICATE_CALENDAR_MRID, "The calendar MRID must be unique.", Level.SEVERE),
    DUPLICATE_CALENDAR_NAME(2019, Constants.DUPLICATE_CALENDAR_NAME, "The calendar name must be unique.", Level.SEVERE),
    IMPORT_FAILED_OTHER_ERROR(2020, Constants.IMPORT_FAILED_OTHER_ERROR, "{0}", Level.SEVERE),
    DESCRIPTION_FIELD_TOO_LONG(2022, Constants.DESCRIPTION_FIELD_TOO_LONG, "Calendar description is too long, it must not exceed 4000 characters.", Level.SEVERE),
    CAL_NAME_FIELD_TOO_LONG(2023, Constants.CAL_NAME_FIELD_TOO_LONG, "Calendar name is too long, it must not exceed 80 characters.", Level.SEVERE),
    CATEGORY_NAME_FIELD_TOO_LONG(2024, Constants.CATEGORY_NAME_FIELD_TOO_LONG, "Category name is too long, it must not exceed 80 characters.", Level.SEVERE),
    DAYTYPE_NAME_FIELD_TOO_LONG(2025, Constants.DAYTYPE_NAME_FIELD_TOO_LONG, "Daytype name is too long, it must not exceed 80 characters.", Level.SEVERE),
    EVENT_NAME_FIELD_TOO_LONG(2026, Constants.EVENT_NAME_FIELD_TOO_LONG, "Event name is too long, it must not exceed 80 characters.", Level.SEVERE),
    PERIOD_NAME_FIELD_TOO_LONG(2027, Constants.PERIOD_NAME_FIELD_TOO_LONG, "Period name is too long, it must not exceed 80 characters.", Level.SEVERE),
    CAL_MRID_FIELD_TOO_LONG(2028, Constants.CAL_MRID_FIELD_TOO_LONG, "Calendar MRID is too long, it must not exceed 80 characters.", Level.SEVERE),
    CAL_TIMEZONE_FIELD_TOO_LONG(2029, Constants.CAL_TIMEZONE_FIELD_TOO_LONG, "Calendar timezone id is too long, it must not exceed 80 characters.", Level.SEVERE),
    VALID_TRANSITIONS(2030, Constants.VALID_TRANSITIONS, "In case of non recurring transitions, a transition at or before the first day of the start year is required.", Level.SEVERE),
    DUPLICATE_CATEGORY_NAME(2031, Constants.DUPLICATE_CATEGORY_NAME, "The category name must be unique.", Level.SEVERE),
    CATEGORY_NOT_FOUND(2032, Constants.CATEGORY_NOT_FOUND, "The category {0} does not exist."),
    EVENTSET_NAME_FIELD_TOO_LONG(2033, Constants.EVENTSET_NAME_FIELD_TOO_LONG, "Event Set name is too long, it must not exceed 80 characters.", Level.SEVERE),
    NO_DAYTYPE_DEFINED_WITH_NAME(2034, "calendar.import.no.daytype.defined.with.name", "There is no day type defined with name \"{0}\"."),
    CANNOT_ADD_PAST_EXCEPTIONS_TO_ACTIVE_CALENDAR(2035, "calendar.update.no.past.exceptions.on.active.calendar", "You can't add fixed exceptional occurrences in the past to an active calendar"),
    ;

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
        return CalendarService.COMPONENTNAME;
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

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public enum Constants {
        ;
        public static final String REQUIRED = "isRequired";
        public static final String DESCRIPTION_FIELD_TOO_LONG = "calendar.description.too.long";
        public static final String DAYTYPES_REQUIRED = "dayTypes.required";
        public static final String PERIODS_REQUIRED = "periods.required";
        public static final String SCHEMA_FAILED = "calendar.import.schema.failed";
        public static final String JAXB_FAILED = "calendar.import.jaxb.failed";
        public static final String PROPERTY_NOT_FOUND_ON_EVENT = "calendar.import.missing.event.property";
        public static final String INVALID_EVENT_CODE = "calendar.import.invalid.event.code";
        public static final String MISSING_CALENDAR_NAME = "calendar.import.missing.calendar.name";
        public static final String MISSING_TIMEZONE = "calendar.import.missing.calendar.timezone";
        public static final String MISSING_STARTYEAR = "calendar.import.missing.calendar.startyear";
        public static final String STARTYEAR_CANNOT_BE_ZERO = "calendar.import.startyear.cannot.be.zero";
        public static final String NO_DAYTYPE_DEFINED_WITH_ID = "calendar.import.no.daytype.defined.with.id";
        public static final String NO_PERIOD_DEFINED_WITH_ID = "calendar.import.no.period.defined.with.id";
        public static final String INVALID_EVENT_ID = "calendar.import.invalid.event.id";
        public static final String NO_EVENT_DEFINED_WITH_ID = "calendar.import.invalid.event.id";
        public static final String NO_TIMEZONE_FOUND_WITH_ID = "calendar.import.no.timezone.found.with.id";
        public static final String YEAR_NOT_ALLOWED_FOR_RECURRING_TRANSITIONS = "calendar.import.year.not.allowed.for.recuring.trainsitions";
        public static final String YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS = "calendar.import.year.required.for.non.recuring.trainsitions";
        public static final String VALIDATION_OF_FILE_SUCCEEDED = "calendar.import.validation succeeded";
        public static final String VALIDATION_OF_FILE_FAILED = "calendar.import.validation.failed";
        public static final String DUPLICATE_CALENDAR_MRID = "calendar.mrid.alreadyexists";
        public static final String DUPLICATE_CALENDAR_NAME = "calendar.name.alreadyexists";
        public static final String DUPLICATE_CATEGORY_NAME = "category.name.alreadyexists";
        public static final String VALIDATION_OF_FILE_FAILED_WITH_DETAIL = "calendar.import.validation.failed.with.detail";
        public static final String IMPORT_FAILED_OTHER_ERROR = "calendar.import.failed.other.error";
        public static final String CALENDAR_CREATED = "calendar.import.calendar.created";
        public static final String CALENDAR_UPDATED = "calendar.import.calendar.updated";
        public static final String CAL_NAME_FIELD_TOO_LONG = "calendar.calendar.name.too.long";
        public static final String EVENTSET_NAME_FIELD_TOO_LONG = "calendar.eventset.name.too.long";
        public static final String CATEGORY_NAME_FIELD_TOO_LONG = "calendar.category.name.too.long";
        public static final String DAYTYPE_NAME_FIELD_TOO_LONG = "calendar.daytype.name.too.long";
        public static final String EVENT_NAME_FIELD_TOO_LONG = "calendar.event.name.too.long";
        public static final String PERIOD_NAME_FIELD_TOO_LONG = "calendar.period.name.too.long";
        public static final String CAL_MRID_FIELD_TOO_LONG = "calendar.mrid.too.long";
        public static final String CAL_TIMEZONE_FIELD_TOO_LONG = "calendar.timezone.too.long";
        public static final String VALID_TRANSITIONS = "calendar.valid.transitions";
        public static final String CATEGORY_NOT_FOUND = "calendar.category.not.found";
    }
}
