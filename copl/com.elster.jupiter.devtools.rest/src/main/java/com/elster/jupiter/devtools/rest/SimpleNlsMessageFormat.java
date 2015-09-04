package com.elster.jupiter.devtools.rest;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Created by bvn on 9/22/14.
 */
class SimpleNlsMessageFormat implements NlsMessageFormat {

    private final String defaultFormat;

    SimpleNlsMessageFormat(TranslationKey translationKey) {
        this.defaultFormat = translationKey.getDefaultFormat();
    }

    SimpleNlsMessageFormat(MessageSeed messageSeed) {
        this.defaultFormat = messageSeed.getDefaultFormat();
    }

    @Override
    public String format(Object... args) {
        return MessageFormat.format(this.defaultFormat, args);
    }

    @Override
    public String format(Locale locale, Object... args) {
        return MessageFormat.format(this.defaultFormat, args);
    }

}