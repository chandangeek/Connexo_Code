package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

class Installer {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());
    private static final String BATCH_EXECUTOR = "batch executor";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final UserService userService;
    private final DataModel dataModel;
    private final MessageService messageService;
    private final Thesaurus thesaurus;

    Installer(UserService userService, DataModel dataModel, MessageService messageService, Thesaurus thesaurus) {
        this.userService = userService;
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.thesaurus = thesaurus;
    }

    public void install() {
        createTables();
        createBatchExecutor();
        createAllServerTopic();
        //createTranslations();
    }

   /* private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(AppService.COMPONENT_NAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }*/

    private void createAllServerTopic() {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(AppService.ALL_SERVERS, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void createBatchExecutor() {
        try {
            User user = userService.createUser(BATCH_EXECUTOR, "User to execute batch tasks.");
            user.save();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void createTables() {
        try {
            dataModel.install(true, true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

}
