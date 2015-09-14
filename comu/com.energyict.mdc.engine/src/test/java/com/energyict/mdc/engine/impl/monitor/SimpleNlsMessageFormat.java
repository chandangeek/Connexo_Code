package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-07 (17:29)
 */
class SimpleNlsMessageFormat implements NlsMessageFormat {
    private final String defaultFormat;

    SimpleNlsMessageFormat(TranslationKey translationKey) {
        this.defaultFormat = translationKey.getDefaultFormat();
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