package com.elster.jupiter.nls;

import java.util.Locale;

public class SimpleTranslation implements Translation {

    private final String translation;
    private final NlsKey nlsKey;
    private final Locale locale;

    private SimpleTranslation(NlsKey nlsKey, Locale locale, String translation) {
        this.translation = translation;
        this.nlsKey = nlsKey;
        this.locale = locale;
    }

    public static Translation translation(NlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
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
