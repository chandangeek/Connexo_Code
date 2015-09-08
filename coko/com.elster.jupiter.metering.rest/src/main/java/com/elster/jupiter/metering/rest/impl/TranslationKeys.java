package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum TranslationKeys implements TranslationKey {
    NO_INTERVAL(Keys.TIME_ATTRIBUTE_KEY_PREFIX + "0", "No interval"),
    NO_MACRO_PERIOD(Keys.MACRO_PERIOD_KEY_PREFIX + "0", "No interval"),
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(Level.INFO, format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(Level.INFO, format.format(args), t);
    }

    public static final class Keys {
        private Keys() {
        }

        public static final String TIME_ATTRIBUTE_KEY_PREFIX = "TimeAttributeId.";
        public static final String MACRO_PERIOD_KEY_PREFIX = "MacroPeriodId.";
    }

}