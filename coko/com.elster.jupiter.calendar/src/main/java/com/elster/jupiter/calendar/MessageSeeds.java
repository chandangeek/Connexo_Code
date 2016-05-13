package com.elster.jupiter.calendar;

import com.elster.jupiter.calendar.importers.impl.CalendarImporterFactory;
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

    SCHEMA_FAILED(2001, Constants.SCHEMA_FAILED, "XSD schema for calendar import could not be read"),
    JAXB_FAILED(2002, Constants.JAXB_FAILED, "JAXB error occurred"),
    PROPERTY_NOT_FOUND_ON_EVENT(2003, Constants.PROPERTY_NOT_FOUND_ON_EVENT, "Missing property \"{0}\" on event"),
    INVALID_EVENT_CODE(2003, Constants.INVALID_EVENT_CODE, "Event code \"{0}\" should be numeric"),
    MISSING_CALENAR_NAME(2004, Constants.MISSING_CALENDAR_NAME, "Calendar name is missing"),
    MISSING_TIMEZONE(2005, Constants.MISSING_TIMEZONE, "Calendar timezone is missing"),
    MISSING_STARTYEAR(2006, Constants.MISSING_STARTYEAR, "Calendar start year is missing"),
    STARTYEAR_CANNOT_BE_ZERO(2007, Constants.STARTYEAR_CANNOT_BE_ZERO, "Calendar start year cannot be zero"),
    NO_DAYTYPE_DEFINED_WITH_ID(2008, Constants.NO_DAYTYPE_DEFINED_WITH_ID, "There is no daytype defined with id \"{0}\""),
    NO_PERIOD_DEFINED_WITH_ID(2009, Constants.NO_PERIOD_DEFINED_WITH_ID, "There is no period defined with id \"{0}\""),
    INVALID_EVENT_ID(2010, Constants.INVALID_EVENT_ID, "Event id \"{0}\" should be numeric"),
    NO_EVENT_DEFINED_WITH_ID(2011, Constants.NO_EVENT_DEFINED_WITH_ID, "There is no daytype defined with id \"{0}\""),
    ENDYEAR_CANNOT_BE_ZERO(2012, Constants.ENDYEAR_CANNOT_BE_ZERO, "Calendar end year cannot be zero"),
    NO_TIMEZONE_FOUND_WITH_ID(2013, Constants.NO_TIMEZONE_FOUND_WITH_ID, "Timzone \"{0}\" does not exist"),
    YEAR_NOT_ALLOWED_FOR_RECURRING_TRANSITIONS(2014, Constants.YEAR_NOT_ALLOWED_FOR_RECURRING_TRANSITIONS, "\"year\" is not allowed for recurring transitions"),
    YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS(2015, Constants.YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS, "\"year\" is required for non recurring transitions"),
    VALIDATION_OF_FILE_SUCCEEDED(2016, Constants.VALIDATION_OF_FILE_SUCCEEDED, "The validation of the file succeeded.", Level.INFO),
    VALIDATION_OF_FILE_FAILED(2017, Constants.VALIDATION_OF_FILE_FAILED, "The validation of the file failed."),
    DUPLICATE_CALENDAR(2018, Constants.DUPLICATE_CALENDAR, "MRID must be unique", Level.SEVERE),
    INVALID_CONTENT(2019, Constants.INVALID_CONTENT, "Please check your file, it contains invalid content.", Level.SEVERE)
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
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
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
        public static final String ENDYEAR_CANNOT_BE_ZERO = "calendar.import.endyear.cannot.be.zero";
        public static final String NO_TIMEZONE_FOUND_WITH_ID = "calendar.import.no.timezone.found.with.id";
        public static final String YEAR_NOT_ALLOWED_FOR_RECURRING_TRANSITIONS = "calendar.import.year.not.allowed.for.recuring.trainsitions";
        public static final String YEAR_REQUIRED_FOR_NOT_RECURRING_TRANSITIONS = "calendar.import.year.required.for.non.recuring.trainsitions";
        public static final String VALIDATION_OF_FILE_SUCCEEDED = "calendar.import.validation succeeded";
        public static final String VALIDATION_OF_FILE_FAILED = "calendar.import.validation.failed";
        public static final String DUPLICATE_CALENDAR = "calendar.mridalreadyexists";
        public static final String INVALID_CONTENT = "calendar.import.invalid.content";

    }
}
