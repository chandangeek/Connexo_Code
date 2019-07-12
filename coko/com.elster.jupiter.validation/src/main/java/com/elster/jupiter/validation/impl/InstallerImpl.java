/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.EventType;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.security.Privileges;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class InstallerImpl implements FullInstaller, PrivilegesProvider {

    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final boolean ENABLE_EXTRA_QUEUE_CREATION = true;
    private static final boolean MAKE_QUEUE_PRIORITIZED = true;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;
    private final UserService userService;

    @Inject
    public InstallerImpl(DataModel dataModel, EventService eventService, MessageService messageService, UserService userService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());

        doTry(
                "Create event types for VAL",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create validation queue",
                this::createMessageHandlers,
                logger
        );
        doTry(
                "Create validation user",
                this::createValidationUser,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return ValidationService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(ValidationService.COMPONENTNAME, Privileges.RESOURCE_VALIDATION.getKey(), Privileges.RESOURCE_VALIDATION_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
                        Privileges.Constants.VALIDATE_MANUAL, Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE,
                        Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION)));
        return resources;
    }

    private void createValidationUser() {
        User validationUser = userService.createUser(ValidationServiceImpl.VALIDATION_USER, ValidationServiceImpl.VALIDATION_USER);
        Optional<Group> batchExecutorRole = userService.findGroup(UserService.BATCH_EXECUTOR_ROLE);
        if (batchExecutorRole.isPresent()) {
            validationUser.join(batchExecutorRole.get());
        }
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }
    void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec(MessageService.PRIORITIZED_RAW_QUEUE_TABLE).get();
        this.createMessageHandler(defaultQueueTableSpec, ValidationServiceImpl.DESTINATION_NAME, TranslationKeys.MESSAGE_SPEC_SUBSCRIBER);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberName) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS, ENABLE_EXTRA_QUEUE_CREATION, MAKE_QUEUE_PRIORITIZED);
            queue.activate();
            queue.subscribe(subscriberName, ValidationService.COMPONENTNAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get().getSubscribers().stream().anyMatch(spec -> spec.getName().equals(subscriberName.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberName, ValidationService.COMPONENTNAME, Layer.DOMAIN);
            }
        }
    }
}
