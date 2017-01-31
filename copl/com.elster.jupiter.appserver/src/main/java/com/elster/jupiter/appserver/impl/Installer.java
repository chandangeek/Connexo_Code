/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.appserver.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class Installer implements FullInstaller, PrivilegesProvider {

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
        userService.addModulePrivileges(this);
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
            Group group = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE).orElseThrow(() -> new IllegalStateException("Couldn't find Batch Executors role"));
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

    @Override
    public String getModuleName() {
        return AppService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_APPSERVER.getKey(), Privileges.RESOURCE_APPSERVER_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_APPSEVER, Privileges.Constants.VIEW_APPSEVER)));
        return resources;
    }


}
