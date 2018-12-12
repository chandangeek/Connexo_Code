/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.kpi.Privileges;
import com.energyict.mdc.device.topology.impl.kpi.RegisteredDevicesKpiCalculatorFactory;
import com.energyict.mdc.device.topology.impl.kpi.TranslationKeys;

import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Represents the Installer for the Device data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (10:48)
 */
public class Installer implements FullInstaller {

    private final DataModel dataModel;
    private final MessageService messageService;
    private final UserService userService;
    private final Logger logger = Logger.getLogger(Installer.class.getName());
    private final EventService eventService;

    public final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    @Inject
    Installer(DataModel dataModel, MessageService messageService, UserService userService, EventService eventService) {
        super();
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.userService = userService;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        for(EventType eventType: EventType.values()) {
            eventType.createIfNotExists(eventService);
        }
        doTry(
                "Create message handlers",
                this::createMessageHandlers,
                logger
        );
        addPrivileges();
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, RegisteredDevicesKpiCalculatorFactory.TASK_DESTINATION, TranslationKeys.REGISTERED_DEVICES_KPI_CALCULATOR);
    }


    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, TopologyService.COMPONENT_NAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = !destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .anyMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, TopologyService.COMPONENT_NAME, Layer.DOMAIN);
            }
        }
    }

    private void addPrivileges() {
        userService.buildResource()
                .component(TopologyService.COMPONENT_NAME)
                .name(Privileges.RESOURCE_REGISTERED_DEVICES_KPI.getKey())
                .description(Privileges.RESOURCE_REGISTERED_DEVICES_KPI_DESCRIPTION.getKey())
                .addPrivilege(Privileges.ADMINISTRATE_REGISTERED_DEVICES_KPI.getKey()).add()
                .addPrivilege(Privileges.VIEW_REGISTERED_DEVICES_KPI.getKey()).add()
                .create();
    }

}