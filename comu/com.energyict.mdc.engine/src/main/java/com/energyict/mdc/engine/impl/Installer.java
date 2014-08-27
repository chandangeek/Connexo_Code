package com.energyict.mdc.engine.impl;

import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.exceptions.MessageSeeds;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs the components of the mdc engine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:08)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final EventService eventService;

    public Installer(DataModel dataModel, Thesaurus thesaurus, EventService eventService) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.createEventTypes();
        this.createTranslations();
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            try {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(EngineService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (!translations.isEmpty()) {
            thesaurus.addTranslations(translations);
        }
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(this.eventService);
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
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