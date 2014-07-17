package com.elster.jupiter.issue.datacollection.impl.i18n;

import com.elster.jupiter.nls.*;

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
                        .key(thesaurus.getComponent(), Layer.DOMAIN, messageSeed.getKey())
                        .defaultMessage(messageSeed.getDefaultFormat());
                translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
            }
            thesaurus.addTranslations(translations);
        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }
    }
}
