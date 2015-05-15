package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.nls.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
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
                        .key(FileImportApplication.COMPONENT_NAME, Layer.REST, messageSeed.getKey())
                        .defaultMessage(messageSeed.getDefaultFormat());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
            }

            thesaurus.addTranslations(translations);
        } catch (Exception e) {
            LOG.log(Level.SEVERE,e.getMessage());
        }
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return SimpleTranslation.translation(nlsKey, locale, translation);
    }


}
