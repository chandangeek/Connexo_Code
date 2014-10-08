package com.elster.jupiter.time.impl;

import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.Privileges;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Installer {
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final UserService userService;

    public Installer(DataModel dataModel, Thesaurus thesaurus, UserService userService) {
        super();
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.userService = userService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        createTranslations();
        createPrivileges(userService);
        assignPrivilegesToDefaultRoles();
    }

    private void createPrivileges(UserService userService) {
        userService.createResourceWithPrivileges("TME", "period.periods", "period.periods.description", new String[]
                {Privileges.VIEW_RELATIVE_PERIOD, Privileges.CREATE_RELATIVE_PERIOD, Privileges.UPDATE_RELATIVE_PERIOD, Privileges.DELETE_RELATIVE_PERIOD});
    }

    private void assignPrivilegesToDefaultRoles() {
        this.userService.grantGroupWithPrivilege(UserService.DEFAULT_METER_EXPERT_ROLE, new String[] {Privileges.CREATE_RELATIVE_PERIOD, Privileges.UPDATE_RELATIVE_PERIOD, Privileges.DELETE_RELATIVE_PERIOD});
        this.userService.grantGroupWithPrivilege(UserService.DEFAULT_METER_OPERATOR_ROLE, new String[] {Privileges.VIEW_RELATIVE_PERIOD});
    }

    private void createTranslations() {
        try {
            List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
            for (MessageSeeds messageSeed : MessageSeeds.values()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(TimeService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
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
