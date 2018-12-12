/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.impl.kpi.RegisteredDevicesKpiCalculatorFactory;
import com.energyict.mdc.device.topology.impl.kpi.TranslationKeys;
import com.energyict.mdc.device.topology.kpi.Privileges;

import javax.inject.Inject;
import java.util.Optional;

public class UpgraderV10_4 implements Upgrader {
    private final DataModel dataModel;
    private final MessageService messageService;
    private final UserService userService;
    private final EventService eventService;

    @Inject
    UpgraderV10_4(DataModel dataModel, MessageService messageService, UserService userService, EventService eventService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.userService = userService;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4));
        createMessageHandlers();
        addPrivileges();
        for(EventType eventType: EventType.values()) {
            eventType.createIfNotExists(eventService);
        }
    }

    private void createMessageHandlers() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandler(defaultQueueTableSpec, RegisteredDevicesKpiCalculatorFactory.TASK_DESTINATION, TranslationKeys.REGISTERED_DEVICES_KPI_CALCULATOR);
    }

    private void createMessageHandler(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, 60);
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
