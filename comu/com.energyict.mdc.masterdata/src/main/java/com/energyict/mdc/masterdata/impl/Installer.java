package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.security.Privileges;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the Installer for the MasterDataService module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:46)
 */
public class Installer {

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final EventService eventService;
    private final MeteringService meteringService;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private final MasterDataService masterDataService;
    private final UserService userService;

    public Installer(DataModel dataModel, Thesaurus thesaurus, EventService eventService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, UserService userService, MasterDataService masterDataService) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        this.userService = userService;
        this.masterDataService = masterDataService;
    }

    public void install(boolean executeDdl, boolean createDefaults) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createEventTypes();
        createTranslations();
        createPrivileges();
        assignPrivilegesToDefaultRoles();
        if (createDefaults) {
            this.createDefaults();
        }
    }

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("MDC", "loadProfileType.loadProfileTypes", "loadProfileType.loadProfileTypes.description", new String[] {Privileges.DELETE_LOAD_PROFILE_TYPE, Privileges.VIEW_LOAD_PROFILE_TYPE});
        this.userService.createResourceWithPrivileges("MDC", "registerGroup.registerGroups", "registerGroup.registerGroups.description", new String[] {Privileges.CREATE_REGISTER_GROUP, Privileges.UPDATE_REGISTER_GROUP, Privileges.DELETE_REGISTER_GROUP, Privileges.VIEW_REGISTER_GROUP});
        this.userService.createResourceWithPrivileges("MDC", "registerType.registerTypes", "registerType.registerTypes.description", new String[] {Privileges.CREATE_REGISTER_TYPE, Privileges.UPDATE_REGISTER_TYPE, Privileges.DELETE_REGISTER_TYPE, Privileges.VIEW_REGISTER_TYPE});
        this.userService.createResourceWithPrivileges("MDC", "phenomenon.phenomenons", "phenomenon.phenomenons.description", new String[] {Privileges.VIEW_PHENOMENON});
    }

    private void assignPrivilegesToDefaultRoles() {
        this.userService.grantGroupWithPrivilege(userService.DEFAULT_METER_EXPERT_ROLE, new String[] {
                Privileges.DELETE_LOAD_PROFILE_TYPE, Privileges.VIEW_LOAD_PROFILE_TYPE,
                Privileges.VIEW_PHENOMENON,
                Privileges.CREATE_REGISTER_GROUP, Privileges.UPDATE_REGISTER_GROUP, Privileges.DELETE_REGISTER_GROUP, Privileges.VIEW_REGISTER_GROUP,
                Privileges.CREATE_REGISTER_TYPE, Privileges.UPDATE_REGISTER_TYPE, Privileges.DELETE_REGISTER_TYPE, Privileges.VIEW_REGISTER_TYPE,
        });
        this.userService.grantGroupWithPrivilege(userService.DEFAULT_METER_OPERATOR_ROLE, new String[] {Privileges.VIEW_LOAD_PROFILE_TYPE, Privileges.VIEW_PHENOMENON});
    }

    private void createDefaults() {
        this.createPhenomena();
        this.createRegisterTypes();
    }

    private void createRegisterTypes() {
        MasterDataGenerator.generateRegisterTypes(meteringService, mdcReadingTypeUtilService, masterDataService);
    }

    private void createPhenomena() {
        try {
            Phenomenon undefined = this.masterDataService.newPhenomenon("Undefined", Unit.getUndefined());
            undefined.save();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        this.generatePhenomenaFromStaticList();
    }

    private void generatePhenomenaFromStaticList() {
        MasterDataGenerator.generatePhenomena(masterDataService);
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            try {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(MasterDataService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
            }
            catch (Exception e) {
                logger.severe(e.getMessage());
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