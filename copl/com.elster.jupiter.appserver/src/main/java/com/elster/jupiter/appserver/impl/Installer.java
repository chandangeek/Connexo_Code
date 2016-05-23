package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

class Installer implements FullInstaller {

    private static final String BATCH_EXECUTOR = "batch executor";
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final UserService userService;
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    Installer(UserService userService, DataModel dataModel, MessageService messageService) {
        this.userService = userService;
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        createTables(dataModelUpgrader);
        createBatchExecutor(logger);
        createAllServerTopic(logger);
    }

    private void createAllServerTopic(Logger logger) {
        try {
            QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWTOPICTABLE").get();
            DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(AppService.ALL_SERVERS, DEFAULT_RETRY_DELAY_IN_SECONDS);
            destinationSpec.activate();
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to create and activate topic for \"All Servers\".", e);
            throw e;
        }
    }

    private void createBatchExecutor(Logger logger) {
        try {
            User user = userService.createUser(BATCH_EXECUTOR, "User to execute batch tasks.");
            user.update();
            Group group = userService.createGroup(UserService.BATCH_EXECUTOR_ROLE, UserService.BATCH_EXECUTOR_ROLE_DESCRIPTION);
            group.update();
            user.join(group);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to create a batch executor user.", e);
            throw e;
        }
    }

    private void createTables(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
    }

}
