/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */


package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.Optional;

import static com.energyict.mdc.device.data.impl.EventType.DEVICE_UPDATED_IPADDRESSV6;

public class UpgraderV10_4_3 implements Upgrader {

    private final DataModel dataModel;
    private final EventService eventService;
    private final MessageService messageService;


    @Inject
    public UpgraderV10_4_3(DataModel dataModel, MessageService messageService, EventService eventService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 3));
        fixIPv6AddressChangeEventType();
    }

    /**
     * Requirement: Have an IPv6 Event with single MRID property
     * <p>
     * Explanation why we need this fix:
     * 10.4.1 Upgrader used to install the IPv6 Event with MRID and IPv6Address properties
     * 10.4.1 Upgrader doesn't install the IPv6 Event any more
     * 10.4.1 and 10.4.2 Installer installs the IPv6 Event with ID property
     */
    private void fixIPv6AddressChangeEventType() {
        Optional<EventType> optionalEventType = eventService.getEventType(DEVICE_UPDATED_IPADDRESSV6.topic());
        if (optionalEventType.isPresent()) {
            EventType eventType = optionalEventType.get();
            this.removeAllPropertyTypes(eventType);
            this.addCustomPropertyType(eventType);
            eventType.update();
        } else {
            DEVICE_UPDATED_IPADDRESSV6.install(eventService);
        }
    }

    private void removeAllPropertyTypes(EventType eventType) {
        eventType.getPropertyTypes().forEach(eventType::removePropertyType);
    }

    private void addCustomPropertyType(EventType eventType) {
        eventType.addProperty("MRID", ValueType.STRING, "MRID");
    }

}