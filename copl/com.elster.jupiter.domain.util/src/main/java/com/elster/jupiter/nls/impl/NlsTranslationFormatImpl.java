package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.google.common.collect.ImmutableMap;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

final class NlsTranslationFormatImpl implements NlsMessageFormat {

    private final NlsString message;
    private final IThesaurus thesaurus;

    NlsTranslationFormatImpl(IThesaurus thesaurus, NlsString message) {
        this.thesaurus = thesaurus;
        this.message = message;
    }

    @Override public String format(Object... args) {
        return new MessageFormat(message.getString(), thesaurus.getLocale()).format(args, new StringBuffer(), null).toString();
    }

    @Override
    public String format(Locale locale, Object... args) {
        return new MessageFormat(message.getString(locale), locale).format(args, new StringBuffer(), null).toString();
    }
}
