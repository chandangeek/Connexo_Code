package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.users.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerImpl {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    public void install(MessageService messageService, UserService userService, Thesaurus thesaurus) {
        //createPrivileges(userService);
        createBPMQueue(messageService, thesaurus);
    }

    private void createBPMQueue(MessageService messageService, Thesaurus thesaurus) {
        try {

            NlsKey statusKey = SimpleNlsKey.key(BpmService.COMPONENTNAME, Layer.DOMAIN, BpmService.BPM_QUEUE_SUBSC);
            Translation statusTranslation = SimpleTranslation.translation(statusKey, Locale.ENGLISH, BpmService.BPM_QUEUE_DISPLAYNAME);
            List<Translation> translations = new ArrayList<>();
            translations.add(statusTranslation);
            thesaurus.addTranslations(translations);

            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(BpmService.BPM_QUEUE_DEST, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
            destinationSpec.subscribe(BpmService.BPM_QUEUE_SUBSC);


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /*
    private void createPrivileges(UserService userService) {
        userService.createResourceWithPrivileges("BPM", "bpm.businessProcesses", "bpm.businessProcesses.description", new String[] {Privileges.DESIGN_BPM});
    }
    */

}
