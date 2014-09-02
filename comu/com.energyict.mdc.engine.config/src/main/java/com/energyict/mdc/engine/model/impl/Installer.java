package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.model.EngineModelService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.model.security.Privileges;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components of the engine configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-26 (08:30)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final EventService eventService;
    private final UserService userService;

    public Installer(DataModel dataModel, Thesaurus thesaurus, EventService eventService, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
        this.userService = userService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.createPrivileges();
        this.assignPrivilegesToDefaultRoles();
        this.createEventTypes();
        this.createTranslations();
    }

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("MDC", "comServer.comServers", "comServer.comServers.description", new String[] {Privileges.CREATE_COMSERVER, Privileges.UPDATE_COMSERVER, Privileges.DELETE_COMSERVER, Privileges.VIEW_COMSERVER});
    }

    private void assignPrivilegesToDefaultRoles() {
        this.userService.grantGroupWithPrivilege(userService.DEFAULT_METER_EXPERT_ROLE, new String[] {Privileges.CREATE_COMSERVER, Privileges.UPDATE_COMSERVER, Privileges.DELETE_COMSERVER, Privileges.VIEW_COMSERVER});
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(EngineModelService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        this.thesaurus.addTranslations(translations);
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(this.eventService);
            }
            catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}