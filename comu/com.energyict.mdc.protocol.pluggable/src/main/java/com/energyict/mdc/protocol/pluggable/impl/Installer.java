package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Installs the protocol pluggable bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (15:39)
 */
public class Installer {

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;

    public Installer(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
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
        createEventTypes();
        createTranslations();
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(ProtocolPluggableService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
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
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
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