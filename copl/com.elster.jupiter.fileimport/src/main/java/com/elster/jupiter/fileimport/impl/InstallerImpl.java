package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.MessageSeeds;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class InstallerImpl {

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final MessageService messageService;
    private final UserService userService;

    InstallerImpl(DataModel dataModel, MessageService messageService, Thesaurus thesaurus, UserService userService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.messageService = messageService;
        this.userService = userService;
    }

    public void install() {

        ExceptionCatcher.executing(
                this::installDataModel,
                this::createTranslations
                //this::createPrivileges
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();

    }


    private void installDataModel() {
        dataModel.install(true, true);
    }

    /*
    private void createPrivileges() {
        userService.createResourceWithPrivileges("SYS", "fileImport.importServices", "fileImport.importServices.description", new String[]
             {Privileges.ADMINISTRATE_IMPORT_SERVICES, Privileges.VIEW_IMPORT_SERVICES});
        userService.
                createResourceWithPrivileges("MDC", "fileImport.importServicesMdc", "fileImport.importServices.description", new String[]
                {Privileges.VIEW_MDC_IMPORT_SERVICES});
    }
    */

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(FileImportService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
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
