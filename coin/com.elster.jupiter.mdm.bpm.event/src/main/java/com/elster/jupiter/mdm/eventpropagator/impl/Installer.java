/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.eventpropagator.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.FullInstaller;

import com.google.inject.Inject;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;

public class Installer implements FullInstaller {

    private final MessageService messageService;
    private final EventService eventService;

    @Inject
    public Installer(MessageService messageService, EventService eventService) {
        this.messageService = messageService;
        this.eventService = eventService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        publishEvents();

        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            if (!jupiterEvents.getSubscribers().stream().anyMatch(s -> s.getName().equals(MeteringMessageHandlerFactory.SUBSCRIBER_NAME))) {
                messageService.getDestinationSpec(EventService.JUPITER_EVENTS).get()
                        .subscribe(MeteringMessageHandlerFactory.SUBSCRIBER_DISPLAYNAME, MeteringMessageHandlerFactory.COMPONENT_NAME, Layer.DOMAIN, whereCorrelationId().isEqualTo(EventType.METERREADING_CREATED
                                .topic())
                                .or(whereCorrelationId().isEqualTo(EventType.METER_UPDATED.topic()))
                                .or(whereCorrelationId().isEqualTo(EventType.USAGEPOINT_UPDATED.topic()))
                                .or(whereCorrelationId().isEqualTo(EventType.METER_ACTIVATED.topic()))
                        );
            }
        }
    }

    private void publishEvents() {
        Stream.of(EventType.METERREADING_CREATED.topic(), EventType.METER_UPDATED.topic(), EventType.USAGEPOINT_UPDATED.topic(), EventType.METER_ACTIVATED
                .topic())
                .map(eventService::getEventType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(eventType -> {
                    eventType.setPublish(true);
                    eventType.update();
                });
    }
}
