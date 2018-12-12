/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest.impl.i18n;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    INVALID_RANGE (1, Keys.INVALID_RANGE, "Invalid date range: start date is later than end date", Level.SEVERE),
    INVALID_TIME_DURATION(2, "InvalidTimeDuration", "Invalid time duration", Level.SEVERE),
    CATEGORY_IN_USE(3, Keys.CATEGORY_IN_USE, "Category ''{0}'' cannot be edited or removed since the category is in use.", Level.SEVERE);

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
        return TimeService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        if (key.startsWith(getModule() + ".")){
            // +1 to skip the dot symbol
            return key.substring(getModule().length() + 1);
        }
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public static class Keys {
        private Keys() {}
        private static final String KEY_PREFIX = TimeService.COMPONENT_NAME + ".";

        public static final String INVALID_RANGE       = KEY_PREFIX + "InvalideRange";
        public static final String CATEGORY_IN_USE = KEY_PREFIX + "CategoryUse";
    }

}
