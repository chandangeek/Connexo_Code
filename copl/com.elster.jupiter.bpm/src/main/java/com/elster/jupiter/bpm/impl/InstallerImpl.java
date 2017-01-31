/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class InstallerImpl implements FullInstaller, PrivilegesProvider {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final String BPM_DESIGNER_ROLE = "Business process designer";
    private static final String BPM_DESIGNER_ROLE_DESCRIPTION = "Business process designer privilege";

    private final DataModel dataModel;
    private final MessageService messageService;
    private final UserService userService;

    @Inject
    InstallerImpl(DataModel dataModel, MessageService messageService, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create BPM queue.",
                this::createBPMQueue,
                logger
        );
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create default roles for BPM",
                this::createDefaultRoles,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return BpmService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        BpmService.COMPONENTNAME,
                        Privileges.RESOURCE_BPM_PROCESSES.getKey(),
                        Privileges.RESOURCE_BPM_PROCESSES_DESCRIPTION.getKey(),
                        Arrays.asList(
                            Privileges.Constants.VIEW_BPM,
                            Privileges.Constants.DESIGN_BPM,
                            Privileges.Constants.ADMINISTRATE_BPM)));
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        BpmService.COMPONENTNAME,
                        Privileges.RESOURCE_BPM_TASKS.getKey(),
                        Privileges.RESOURCE_BPM_TASKS_DESCRIPTION.getKey(),
                        Arrays.asList(
                            Privileges.Constants.ASSIGN_TASK,
                            Privileges.Constants.VIEW_TASK,
                            Privileges.Constants.EXECUTE_TASK)));
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        BpmService.COMPONENTNAME,
                        Privileges.PROCESS_EXECUTION_LEVELS.getKey(),
                        Privileges.PROCESS_EXECUTION_LEVELS_DESCRIPTION.getKey(),
                        Arrays.asList(
                            Privileges.Constants.EXECUTE_PROCESSES_LVL_1,
                            Privileges.Constants.EXECUTE_PROCESSES_LVL_2,
                            Privileges.Constants.EXECUTE_PROCESSES_LVL_3,
                            Privileges.Constants.EXECUTE_PROCESSES_LVL_4)));

        return resources;
    }

    private void createBPMQueue() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(BpmServiceImpl.BPM_QUEUE_DEST, DEFAULT_RETRY_DELAY_IN_SECONDS);
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.QUEUE_SUBSCRIBER, BpmService.COMPONENTNAME, Layer.DOMAIN);
    }

    private void createDefaultRoles() {
        Group group = userService.createGroup(BPM_DESIGNER_ROLE, BPM_DESIGNER_ROLE_DESCRIPTION);
        userService.grantGroupWithPrivilege(group.getName(), BpmService.COMPONENTNAME, new String[]{"privilege.design.bpm"});
    }

}
