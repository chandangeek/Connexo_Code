package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.exceptions.MessageSeeds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (09:40)
 */
public class Installer {

    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    public Installer(DataModel dataModel, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    public void install(boolean executeDdl, boolean createMasterData) {
        try {
            this.dataModel.install(executeDdl, false);
            if (createMasterData) {
                this.createMasterData();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.createEventTypes();
        this.createTranslations();
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(RelationService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }


    private void createMasterData() {
    }

    private void createEventTypes() {
    }

    private static class SimpleTranslation implements Translation {
        private final SimpleNlsKey nlsKey;
        private final Locale locale;
        private final String translation;

        private SimpleTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
            super();
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