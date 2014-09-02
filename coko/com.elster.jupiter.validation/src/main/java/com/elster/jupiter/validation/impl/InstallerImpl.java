package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.MessageSeeds;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.security.Privileges;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl {
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private volatile Thesaurus thesaurus;

    public InstallerImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, UserService userService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
        this.userService = userService;
    }

    public void install(boolean executeDdl, boolean updateOrm) {
        try {
            dataModel.install(executeDdl, updateOrm);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not install datamodel : " + ex.getMessage(), ex);
        }
        createPrivileges();
        assignPrivilegesToDefaultRoles();
        setTranslations();
        createEventTypes();
    }

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("SYS", "validationRule.validationRules", "validationRule.validationRules.description", new String[] {Privileges.CREATE_VALIDATION_RULE, Privileges.UPDATE_VALIDATION_RULE, Privileges.DELETE_VALIDATION_RULE, Privileges.VIEW_VALIDATION_RULE, Privileges.ACTIVATE_VALIDATION_RULE});
    }

    private void assignPrivilegesToDefaultRoles() {
        this.userService.grantGroupWithPrivilege(userService.DEFAULT_METER_EXPERT_ROLE, new String[] {Privileges.VIEW_VALIDATION_RULE, Privileges.CREATE_VALIDATION_RULE, Privileges.UPDATE_VALIDATION_RULE, Privileges.DELETE_VALIDATION_RULE, Privileges.ACTIVATE_VALIDATION_RULE});
        this.userService.grantGroupWithPrivilege(userService.DEFAULT_METER_OPERATOR_ROLE, new String[] {Privileges.VIEW_VALIDATION_RULE});
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(eventService);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not install eventType '" + eventType.name() + "': " + ex.getMessage(), ex);
            }
        }
    }

    private void setTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(ValidationService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        try {
            thesaurus.addTranslations(translations);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not install translations : " + ex.getMessage(), ex);
        }
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
