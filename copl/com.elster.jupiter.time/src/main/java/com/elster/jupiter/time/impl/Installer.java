package com.elster.jupiter.time.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.EventType;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

class Installer implements FullInstaller {
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final TimeService timeService;

    @Inject
    Installer(DataModel dataModel, TimeService timeService, EventService eventService) {
        super();
        this.timeService = timeService;
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader) {
        ExceptionCatcher.executing(
                () -> dataModelUpgrader.upgrade(dataModel, Version.latest()),
                this::createEventTypes,
                this::createDefaultRelativePeriods
        ).andHandleExceptionsWith(e -> logger.log(Level.SEVERE, e.getMessage(), e))
                .execute();
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
