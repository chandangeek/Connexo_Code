package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.config.security.Privileges;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

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
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createEventTypes();
        createTranslations();
        createPrivileges();
        createDTCPrivileges();
    }

    private void createPrivileges() {
        try {
            this.userService.createResourceWithPrivileges("MDC", "masterData.masterData", "masterData.masterData.description", new String[] {Privileges.ADMINISTRATE_MASTER_DATA, Privileges.VIEW_MASTER_DATA});
            this.userService.createResourceWithPrivileges("MDC", "deviceType.deviceTypes", "deviceType.deviceTypes.description", new String[] {Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE});
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void createDTCPrivileges() {
        List<String> collect = Arrays.asList(DeviceSecurityUserAction.values()).stream().map(DeviceSecurityUserAction::getPrivilege).collect(toList());
        //collect.addAll(Arrays.asList(DeviceMessageUserAction.values()).stream().map(DeviceMessageUserAction::getPrivilege).collect(toList()));
        this.userService.createResourceWithPrivileges("MDC", "deviceSecurity.deviceSecurities", "deviceSecurity.deviceSecurities.description", collect.toArray(new String[collect.size()]));
        List<String> collectDeviceUserMessages = Arrays.asList(DeviceMessageUserAction.values()).stream().map(DeviceMessageUserAction::getPrivilege).collect(toList());
        this.userService.createResourceWithPrivileges("MDC", "deviceCommand.deviceCommands", "deviceCommand.deviceCommands.description", collectDeviceUserMessages.toArray(new String[collectDeviceUserMessages.size()]));
    }

    private void createTranslations() {
        try {
            List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
            for (MessageSeeds messageSeed : MessageSeeds.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(DeviceConfigurationService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
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
            try {
                eventType.install(this.eventService);
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