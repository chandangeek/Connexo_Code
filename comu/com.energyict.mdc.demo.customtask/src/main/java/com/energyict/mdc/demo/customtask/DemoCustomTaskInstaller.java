/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.demo.customtask;

import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.DynamicPrivilegesRegister;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.demo.customtask.impl.DemoCustomTaskFactory;
import com.energyict.mdc.demo.customtask.impl.TranslationKeys;
import com.energyict.mdc.demo.customtask.security.Privileges;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.demo.customtask",
        service = {DemoCustomTaskInstaller.class, DynamicPrivilegesRegister.class, TranslationKeyProvider.class},
        immediate = true,
        property = "name=MCT"
        )
public class DemoCustomTaskInstaller implements FullInstaller, PrivilegesProvider, DynamicPrivilegesRegister, TranslationKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(DemoCustomTaskInstaller.class.getName());
    private volatile UpgradeService upgradeService;
    private volatile MessageService messageService;
    private volatile UserService userService;

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DemoCustomTaskInstaller.class).toInstance(DemoCustomTaskInstaller.this);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("MultiSense", "MCT"), dataModel, DemoCustomTaskInstaller.class, Collections.emptyMap());
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        doTry(
                "Create custom task queue for " + DemoCustomTaskFactory.NAME,
                this::createDestinationAndSubscriber,
                logger
        );
        userService.addModulePrivileges(this);
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(DemoCustomTaskFactory.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.SUBSCRIBER_NAME, CustomTaskService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public String getModuleName() {
        return CustomTaskService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_DEMO_CUSTOM_TASK.getKey(), Privileges.RESOURCE_DEMO_CUSTOM_TASK_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_MDC_DEMO_CUSTOM_TASK,
                        Privileges.Constants.VIEW_MDC_DEMO_CUSTOM_TASK,
                        Privileges.Constants.RUN_MDC_DEMO_CUSTOM_TASK)));
        return resources;
    }

    @Override
    public List<String> getPrivileges(String application){
        switch (application) {
            case "MDC":
                return Arrays.asList(Privileges.Constants.ADMINISTRATE_MDC_DEMO_CUSTOM_TASK,
                        Privileges.Constants.VIEW_MDC_DEMO_CUSTOM_TASK,
                        Privileges.Constants.RUN_MDC_DEMO_CUSTOM_TASK);
            case "INS":
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Stream.of(TranslationKeys.values()),
                Stream.of(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public String getComponentName() {
        return CustomTaskService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

}