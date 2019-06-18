/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.Privileges;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components of the finite state machine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:57)
 */
public class Installer implements FullInstaller, PrivilegesProvider {

    private final DataModel dataModel;
    private final EventService eventService;
    private final FiniteStateMachineService finiteStateMachineService;
    private volatile MessageService messageService;
    private final UserService userService;

    @Inject
    public Installer(DataModel dataModel, EventService eventService, FiniteStateMachineService finiteStateMachineService, UserService userService, MessageService messageService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create event types for FSM",
                this::createEventTypes,
                logger
        );
        doTry(
                "Create destination spec",
                this::createDestinationAndSubscriber,
                logger
        );
        userService.addModulePrivileges(this);
    }

    @Override
    public String getModuleName() {
        return FiniteStateMachineService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(FiniteStateMachineService.COMPONENT_NAME,
                Privileges.RESOURCE_FSM.getKey(), Privileges.RESOURCE_FSM_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.CONFIGURE_FINITE_STATE_MACHINES, Privileges.Constants.VIEW_FINITE_STATE_MACHINES)));
        return resources;
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(this.eventService);
        }
    }

    private void createDestinationAndSubscriber() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec(InitialStateActionsMessageHandlerFactory.DESTINATION_NAME, 60);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe(TranslationKeys.SUBSCRIBER_NAME, FiniteStateMachineService.COMPONENT_NAME, Layer.DOMAIN);
    }
}