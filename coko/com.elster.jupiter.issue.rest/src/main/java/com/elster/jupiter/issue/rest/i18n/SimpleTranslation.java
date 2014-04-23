package com.elster.jupiter.issue.rest.i18n;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Translation;

import java.util.Locale;

public class SimpleTranslation implements Translation {
    private final SimpleNlsKey nlsKey;
    private final Locale locale;
    private final String translation;

    public SimpleTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        this.nlsKey = nlsKey;
        this.locale = locale;
        this.translation = translation;
    }

    @Override
    public NlsKey getNlsKey() {
        return nlsKey;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getTranslation() {
        return translation;
    }
}
