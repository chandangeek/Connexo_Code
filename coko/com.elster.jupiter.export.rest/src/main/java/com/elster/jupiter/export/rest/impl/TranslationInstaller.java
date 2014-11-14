package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class TranslationInstaller {
    private static final Logger LOG = Logger.getLogger(TranslationInstaller.class.getName());

    private Thesaurus thesaurus;

    public TranslationInstaller(Thesaurus thesaurus) {
        if (thesaurus == null) {
            throw new IllegalArgumentException("Thesaurus can't be null");
        }
        this.thesaurus = thesaurus;
    }

    public void createTranslations() {
        try {
            List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
            for (MessageSeeds messageSeed : MessageSeeds.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey
                        .key(thesaurus.getComponent(), Layer.REST, messageSeed.getKey())
                        .defaultMessage(messageSeed.getDefaultFormat());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
            }
            thesaurus.addTranslations(translations);
        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }


}

class SimpleTranslation implements Translation {
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
