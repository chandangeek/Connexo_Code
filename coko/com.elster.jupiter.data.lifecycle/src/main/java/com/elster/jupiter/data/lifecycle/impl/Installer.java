/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.data.lifecycle.LifeCycleService;
import com.elster.jupiter.data.lifecycle.security.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class Installer implements FullInstaller, PrivilegesProvider {

    static final String DATA_LIFE_CYCLE_DESTINATION_NAME = "DataLifeCycle";
    static final String DATA_LIFE_CYCLE_DISPLAY_NAME = "Handle purge data";
    private static final String DATA_LIFECYCLE_RECCURENT_TASK_NAME = "Data Lifecycle";

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final MessageService messageService;
    private final TaskService taskService;
    private final MeteringService meteringService;
    private final UserService userService;

    @Inject
    Installer(DataModel dataModel, Thesaurus thesaurus, MessageService messageService, TaskService taskService, MeteringService meteringService, UserService userService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.messageService = messageService;
        this.taskService = taskService;
        this.meteringService = meteringService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        for (LifeCycleCategoryKind category : LifeCycleCategoryKind.values()) {
            LifeCycleCategory newCategory = new LifeCycleCategoryImpl(dataModel, thesaurus, meteringService).init(category);
            try {
                dataModel.persist(newCategory);
            } catch (UnderlyingSQLFailedException ex){
                logger.warning("The LifeCycleCategory '" + newCategory.getName() + "' already exists");
                throw ex;
            }
        }
        createTask();
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return LifeCycleService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(LifeCycleService.COMPONENTNAME, Privileges.RESOURCE_DATA_PURGE.getKey(), Privileges.RESOURCE_DATA_PURGE_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_DATA_PURGE, Privileges.Constants.VIEW_DATA_PURGE)));
        return resources;
    }

    private DestinationSpec getDestination() {
        return messageService.getDestinationSpec(DATA_LIFE_CYCLE_DESTINATION_NAME).orElseGet(() ->
                messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get().createDestinationSpec(DATA_LIFE_CYCLE_DESTINATION_NAME, 10));
    }

    private SubscriberSpec getSubscriberSpec() {
        return getDestination()
                .getSubscribers()
                .stream()
                .findFirst()
                .orElseGet(() -> getDestination().subscribe(TranslationKeys.DATA_LIFE_CYCLE, LifeCycleService.COMPONENTNAME, Layer.DOMAIN));
    }

    private void createTask() {
        if (!taskService.getRecurrentTask(DATA_LIFECYCLE_RECCURENT_TASK_NAME).isPresent()) {
            taskService.newBuilder()
                    .setApplication("Admin")
                    .setName(DATA_LIFECYCLE_RECCURENT_TASK_NAME)
                    .setScheduleExpressionString("0 0 18 ? * 1L") // last sunday of the month at 18:00
                    .setDestination(getDestination())
                    .setPayLoad("Data Lifecycle")
                    .scheduleImmediately(true)
                    .build();
        }
        DestinationSpec destination = getDestination();
        if (!destination.isActive()) {
            destination.activate();
        }
        getSubscriberSpec();
    }

}

