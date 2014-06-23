package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final Thesaurus thesaurus;

    public Installer(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    public void install() {
        createTranslations();
    }

    private void createTranslations() {
        try {
            List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
            for (MessageSeeds messageSeed : MessageSeeds.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(DeviceApplication.COMPONENT_NAME, messageSeed.getLayer(), messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
            }
            thesaurus.addTranslations(translations);
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }

    private static class SimpleTranslation implements Translation {

        private final SimpleNlsKey nlsKey;
        private final Locale locale;
        private final String translation;

        private SimpleTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
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

}