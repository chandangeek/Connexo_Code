package com.elster.jupiter.metering;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    ILLEGAL_MRID_FORMAT(1001, "mrid.illegalformat", "Supplied MRID ''{0}'' is not the correct format.", Level.SEVERE),
    ILLEGAL_CURRENCY_CODE(1002, "currency.illegalcode", "Invalid currency code : ''{0}''", Level.SEVERE),
    METER_EVENT_IGNORED(2001, "meter.event.ignored", "Ignored event {0} on meter {1}, since it is not defined in the system", Level.INFO),
    READINGTYPE_IGNORED(2002, "readingtype.ignored", "Ignored data for reading type {0} on meter {1} , since reading type is not defined int the system", Level.INFO),
    NOMETERACTIVATION(2003, "meter.nometeractivation", "No meter activation found for meter {0} on {1} ", Level.INFO),
    READINGTYPE_ADDED(2004, "readingtype.added", "Added reading type {0} for meter {1} ", Level.INFO),
    CANNOT_DELETE_METER_METER_ACTIVATIONS_EXIST(2005, "meter.cannot.delete.with.activations", "Cannot delete meter {0} because meter activations are linked to the meter", Level.SEVERE),
    READING_TIMESTAMP_NOT_IN_MEASUREMENT_PERIOD(2006, "reading.timesatmp.not.in.measurement.period", "Measurement time should be in measurement period", Level.SEVERE),
    METER_ALREADY_ACTIVE(2007, "meter.alreadyactive", "Meter {0} is already active at {1}", Level.SEVERE),
    METER_ALREADY_LINKED_TO_USAGEPOINT(2008, "meter.alreadyhasusagepoint", "Meter {0} is already linked to a usage point {1}, cannot link to another.", Level.SEVERE),
    TOO_MANY_READINGTYPES(2009, "readingtype.tooManyReadingTypes", "You are going to add {0} reading types. The limit is 1000.", Level.SEVERE),
    READINGTYPE_ALREADY_EXISTS(2010, "readingtype.alreadyExists", "Reading type {0} already exists.", Level.SEVERE),
    READINGTYPE_CREATING_FAIL(2011, "readingtype.creatingFail", "Failed creating reading types.", Level.SEVERE),
    FIELD_NOT_FOUND(2012, "readingtype.fieldNotFound", "Cannot find field {0}", Level.SEVERE),

    DUPLICATE_USAGEPOINT(3001, Constants.DUPLICATE_USAGEPOINT, "MRID must be unique", Level.SEVERE),

    REQUIRED(4001, Constants.REQUIRED, "This field is required"),
    FAIL_MANAGE_CPS_ON_ACTIVE_METROLOGY_CONFIGURATION(4002, Constants.FAIL_MANAGE_CPS_ON_ACTIVE_METROLOGY_CONFIGURATION, "You cannot manage custom attribute sets because metrology configuration is active."),
    OBJECT_MUST_HAVE_UNIQUE_NAME(4003, Constants.OBJECT_MUST_HAVE_UNIQUE_NAME, "Name must be unique"),
    CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER(4004, Constants.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, "The custom attribute set ''{0}'' is not editable by current user."),
    NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT(4005, Constants.NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT, "The custom attribute set ''{0}'' is not attached to the usage point."),
    CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN(4006, Constants.CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN, "The custom attribute set ''{0}'' has different domain type."),
    CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED(4007, Constants.CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED, "The custom attribute set ''{0}'' is not versioned."),;

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
        return MeteringService.COMPONENTNAME;
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
        public static final String DUPLICATE_USAGEPOINT = "usagepoint.mridalreadyexists";
        public static final String FAIL_MANAGE_CPS_ON_ACTIVE_METROLOGY_CONFIGURATION = "fail.manage.cps.on.active.metrology.configuration";
        public static final String OBJECT_MUST_HAVE_UNIQUE_NAME = "name.must.be.unique";
        public static final String REQUIRED = "isRequired";
        public static final String LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED = "lastChecked.after.currentLastChecked";
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER = "custom.property.set.is.not.editable.by.user";
        public static final String NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT = "no.linked.custom.property.set.on.usage.point";
        public static final String CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN = "custom.property.set.has.different.domain";
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_VERSIONED = "custom.property.set.is.not.versioned";
    }

}