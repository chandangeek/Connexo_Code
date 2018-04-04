/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceDataServices;

import javax.inject.Inject;
import java.util.Optional;

class UpgraderV10_4_1 implements Upgrader {
    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;

    @Inject
    UpgraderV10_4_1(DataModel dataModel, EventService eventService, MessageService messageService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 1));
        installNewEventTypeAndPublish();
        createSubscriberForMessageQueue();
    }

    private void installNewEventTypeAndPublish() {
        // EventType.DEVICE_UPDATED_IPADDRESSV6.install(eventService);
        // metoda asta nu are publish. Din acest motiv nu am folosit-o si am scris alta
        //la care am adaugat shouldPublish.
        if (!this.eventService.getEventType(EventType.DEVICE_UPDATED_IPADDRESSV6.topic()).isPresent()) {
            EventTypeBuilder builder = eventService.buildEventTypeWithTopic(EventType.DEVICE_UPDATED_IPADDRESSV6.topic())
                    .name(EventType.DEVICE_UPDATED_IPADDRESSV6.name())
                    .component(DeviceDataServices.COMPONENT_NAME)
                    .category("Crud")
                    .scope("System");
            this.addCustomProperties(builder).create();
        }
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        eventTypeBuilder
                .withProperty("MRID", ValueType.STRING, "MRID")
                .withProperty("IPv6Address", ValueType.STRING, "IPv6Address")
                .shouldPublish();
        return eventTypeBuilder;
    }

    private void doSubscriber(DestinationSpec jupiterEvents, Pair<SubscriberTranslationKeys, Condition> subscriber) {
        jupiterEvents.subscribe(subscriber.getFirst(), DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN, subscriber.getLast());
    }

   public static final TranslationKey IPV6ADDRESS_SUBSCRIBER_DISPLAYNAME =
            new SimpleTranslationKey("IPv6AddressSubscriber",
                    "Handle events to propagate ipv6 change address into MSG_RAWTOPICTABLE");  //lori

    private void createSubscriberForMessageQueue() {
        Optional<DestinationSpec> destinationSpec = this.messageService.getDestinationSpec(EventService.JUPITER_EVENTS);
        if (destinationSpec.isPresent()) {
            DestinationSpec jupiterEvents = destinationSpec.get();
            if (!jupiterEvents.getSubscribers().stream()
                    .anyMatch(s -> s.getName().equals(IPV6ADDRESS_SUBSCRIBER_DISPLAYNAME.getKey()))) {
                jupiterEvents
                        .subscribe(IPV6ADDRESS_SUBSCRIBER_DISPLAYNAME,
                                DeviceDataServices.COMPONENT_NAME,
                                Layer.DOMAIN,
                                DestinationSpec.whereCorrelationId()
                                        .isEqualTo(EventType.DEVICE_UPDATED_IPADDRESSV6.topic()));
            }
        }
    }
}
