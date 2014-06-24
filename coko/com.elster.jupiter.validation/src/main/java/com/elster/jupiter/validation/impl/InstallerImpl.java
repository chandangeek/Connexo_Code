package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InstallerImpl {

    private final DataModel dataModel;
    private final EventService eventService;
    private volatile Thesaurus thesaurus;

    public InstallerImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    public void install(boolean executeDdl, boolean updateOrm) {
        dataModel.install(executeDdl, updateOrm);
        setTranslations();
        createEventTypes();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

    private void setTranslations(){
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(ValidationService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private Translation toTranslation(final SimpleNlsKey nlsKey, final Locale locale, final String translation) {
        return new Translation() {
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
        };
    }
}
