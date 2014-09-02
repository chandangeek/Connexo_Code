package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.events.EventType;
import com.energyict.mdc.scheduling.security.Privileges;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Installer for the DeviceConfiguration module
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:42)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;
    private final UserService userService;

    public Installer(DataModel dataModel, EventService eventService, Thesaurus thesaurus, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.userService = userService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createPrivileges();
        assignPrivilegesToDefaultRoles();
        createEventTypes();
        createTranslations();
    }

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("MDC", "schedule.schedules", "schedule.schedules.description", new String[] {Privileges.CREATE_SCHEDULE, Privileges.UPDATE_SCHEDULE, Privileges.DELETE_SCHEDULE, Privileges.VIEW_SCHEDULE});
    }

    private void assignPrivilegesToDefaultRoles() {
        this.userService.grantGroupWithPrivilege(userService.DEFAULT_METER_EXPERT_ROLE, new String[] {Privileges.CREATE_SCHEDULE, Privileges.UPDATE_SCHEDULE, Privileges.DELETE_SCHEDULE, Privileges.VIEW_SCHEDULE});
    }

    private void createTranslations() {
        try {
            List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
            for (MessageSeeds messageSeed : MessageSeeds.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(SchedulingService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
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

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            install(eventType);
        }
    }

    @TransactionRequired
    void install(EventType eventType) {
        if (!eventService.getEventType(eventType.topic()).isPresent()) {
            EventTypeBuilder eventTypeBuilder = this.eventService.buildEventTypeWithTopic(eventType.topic())
                    .name(eventType.name())
                    .component(SchedulingService.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System")
                    .shouldPublish();
            eventType.addCustomProperties(eventTypeBuilder);
            eventTypeBuilder.create().save();
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