/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;


import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.Optional;

public class UpgraderV10_6 implements Upgrader {
    private final int DEFAULT_RETRY_DELAY_IN_SECONDS = 60;

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;

    @Inject
    public UpgraderV10_6(DataModel dataModel, EventService eventService, MessageService messageService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 6));
        upgradeSubscriberSpecs();
        createNewEventType();
    }

    private void upgradeSubscriberSpecs() {
        QueueTableSpec defaultQueueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        this.createMessageHandlerIfNotPresent(defaultQueueTableSpec, InitialStateActionsMessageHandlerFactory.DESTINATION_NAME, TranslationKeys.SUBSCRIBER_NAME);
    }

    private void createMessageHandlerIfNotPresent(QueueTableSpec defaultQueueTableSpec, String destinationName, TranslationKey subscriberKey) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = defaultQueueTableSpec.createDestinationSpec(destinationName, DEFAULT_RETRY_DELAY_IN_SECONDS);
            queue.activate();
            queue.subscribe(subscriberKey, FiniteStateMachineService.COMPONENT_NAME, Layer.DOMAIN);
        } else {
            boolean notSubscribedYet = destinationSpecOptional.get()
                    .getSubscribers()
                    .stream()
                    .noneMatch(spec -> spec.getName().equals(subscriberKey.getKey()));
            if (notSubscribedYet) {
                destinationSpecOptional.get().activate();
                destinationSpecOptional.get().subscribe(subscriberKey, FiniteStateMachineService.COMPONENT_NAME, Layer.DOMAIN);
            }
        }
    }

    private void createNewEventType() {
        EventType.STATE_INIT.install(eventService);
    }
}