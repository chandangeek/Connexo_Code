/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.validation.EventType;

import com.google.inject.Inject;

import java.util.Arrays;

public class UpgraderV10_8_1 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;

    @Inject
    UpgraderV10_8_1(DataModel dataModel, EventService eventService, MessageService messageService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 8, 1));
        upgradeSuspectCreatedEvent();
    }

    private void upgradeSuspectCreatedEvent() {
        eventService.getEventType(EventType.SUSPECT_VALUE_CREATED.topic())
                .ifPresent(eventType -> {
                    messageService.getDestinationSpec(EventService.JUPITER_EVENTS)
                            .ifPresent(destinationSpec -> destinationSpec.purgeCorrelationId(eventType.getTopic()));

                    eventType.getPropertyTypes().forEach(eventType::removePropertyType);
                    Arrays.stream(SuspectsCreatedEvent.class.getDeclaredFields())
                            .forEach(field -> eventType.addProperty(field.getName(), ValueType.valueOf(field.getType()).get(), field.getName()));
                    eventType.update();
                });
    }
}
