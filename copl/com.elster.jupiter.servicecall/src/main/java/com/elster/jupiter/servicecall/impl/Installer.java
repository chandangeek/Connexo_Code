/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.security.Privileges;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 3/7/16.
 */
public class Installer implements FullInstaller, PrivilegesProvider {
    private static final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;
    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private final MessageService messageService;
    private final ServiceCallService serviceCallService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final DataModel dataModel;
    private final UserService userService;

    @Inject
    public Installer(MessageService messageService, ServiceCallService serviceCallService, FiniteStateMachineService finiteStateMachineService, DataModel dataModel, UserService userService) {
        this.messageService = messageService;
        this.serviceCallService = serviceCallService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.dataModel = dataModel;
        this.userService = userService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        createMessageHandler(defaultQueueTableSpec, ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME, TranslationKeys.SERVICE_CALL_SUBSCRIBER, logger);
        doTry(
                "Install default Service Call Life Cycle.",
                this::installDefaultLifeCycle,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return ServiceCallService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        Privileges.RESOURCE_SERVICE_CALL_TYPES.getKey(),
                        Privileges.RESOURCE_SERVICE_CALL_TYPES_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES,
                                Privileges.Constants.VIEW_SERVICE_CALL_TYPES)));
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        Privileges.RESOURCE_SERVICE_CALL.getKey(),
                        Privileges.RESOURCE_SERVICE_CALL_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.VIEW_SERVICE_CALLS,
                                Privileges.Constants.CHANGE_SERVICE_CALL_STATE)));
        return resources;
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberName, Logger logger) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = doTry(
                    "Create Queue : " + ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME,
                    () -> {
                        DestinationSpec destinationSpec = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
                        destinationSpec.activate();
                        return destinationSpec;
                    },
                    logger
            );
            doTry(
                    "Create subsriber " + ServiceCallServiceImpl.SERVICE_CALLS_SUBSCRIBER_NAME + " on " + ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME,
                    () -> queue.subscribe(TranslationKeys.SERVICE_CALL_SUBSCRIBER, ServiceCallService.COMPONENT_NAME, Layer.DOMAIN),
                    logger
            );
        } else {
            DestinationSpec queue = destinationSpecOptional.get();
            boolean notSubscribedYet = queue
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberName));
            if (notSubscribedYet) {
                doTry(
                        "Create subsriber " + ServiceCallServiceImpl.SERVICE_CALLS_SUBSCRIBER_NAME + " on " + ServiceCallServiceImpl.SERVICE_CALLS_DESTINATION_NAME,
                        () -> {
                            queue.activate();
                            queue.subscribe(subscriberName, ServiceCallService.COMPONENT_NAME, Layer.DOMAIN);
                        },
                        logger
                );
            }
        }

    }

    public void installDefaultLifeCycle() {
        Map<String, CustomStateTransitionEventType> eventTypes = this.findOrCreateStateTransitionEventTypes();
        this.createDefaultLifeCycle(
                TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME.getKey()
        );
    }

    private void createDefaultLifeCycle(String name) {
        serviceCallService.getDefaultServiceCallLifeCycle()
                .orElseGet(() -> serviceCallService.createServiceCallLifeCycle(name).create());
    }

    private Map<String, CustomStateTransitionEventType> findOrCreateStateTransitionEventTypes() {
        // Create default StateTransitionEventTypes
        LOGGER.fine(() -> "Finding (or creating) default finite state machine transitions...");
        Map<String, CustomStateTransitionEventType> eventTypes = Stream
                .of(DefaultCustomStateTransitionEventType.values())
                .map(each -> each.findOrCreate(this.finiteStateMachineService))
                .collect(Collectors.toMap(
                        StateTransitionEventType::getSymbol,
                        Function.identity()));
        LOGGER.fine(() -> "Found (or created) default finite state machine transitions");
        return eventTypes;
    }

}
