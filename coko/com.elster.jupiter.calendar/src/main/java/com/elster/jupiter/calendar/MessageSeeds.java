package com.elster.jupiter.calendar;

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
    }
}
