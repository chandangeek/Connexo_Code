package com.elster.jupiter.time.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.EventType;
import com.elster.jupiter.time.TimeService;
//import com.elster.jupiter.time.security.Privileges;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Installer {
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final UserService userService;
    private final EventService eventService;
    private final TimeService timeService;

    public Installer(DataModel dataModel, TimeService timeService, Thesaurus thesaurus, UserService userService, EventService eventService) {
        super();
        this.timeService = timeService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.userService = userService;
        this.eventService = eventService;
    }

    public void install(boolean executeDdl) {
        ExceptionCatcher.executing(
                () -> this.dataModel.install(executeDdl, true),
                this::createMessageSeedTranslations,
                this::createLabelTranslations,
                //this::createPrivileges,
                this::createEventTypes,
                this::createDefaultRelativePeriods
        ).andHandleExceptionsWith(e -> logger.log(Level.SEVERE, e.getMessage(), e))
                .execute();
    }
    /*
    private void createPrivileges() {
        userService.createResourceWithPrivileges("SYS", "period.periods", "period.periods.description", new String[]
                {Privileges.VIEW_RELATIVE_PERIOD, Privileges.ADMINISTRATE_RELATIVE_PERIOD});
    }
    */
    private void createMessageSeedTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(TimeService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private void createLabelTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (Labels label : Labels.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(TimeService.COMPONENT_NAME, Layer.DOMAIN, label.getKey()).defaultMessage(label.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, label.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private void createDefaultRelativePeriods() {
        Arrays.stream(DefaultRelativePeriodDefinition.values())
                .forEach(definition -> definition.create(timeService, Collections.emptyList()));
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.createIfNotExists(this.eventService);
            } catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
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
