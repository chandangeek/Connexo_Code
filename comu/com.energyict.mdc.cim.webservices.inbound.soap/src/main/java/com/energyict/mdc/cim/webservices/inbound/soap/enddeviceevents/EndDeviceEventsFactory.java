/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;

import java.util.ArrayList;
import java.util.List;

class EndDeviceEventsFactory {
    EndDeviceEvents asEndDeviceEvents(com.elster.jupiter.metering.readings.EndDeviceEvent createdEndDeviceEvent) {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent(createdEndDeviceEvent);
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        return endDeviceEvents;
    }

    EndDeviceEvent createEndDeviceEvent(com.elster.jupiter.metering.readings.EndDeviceEvent createdEndDeviceEvent) {
        EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
        endDeviceEvent.setMRID(createdEndDeviceEvent.getMRID());
        endDeviceEvent.setCreatedDateTime(createdEndDeviceEvent.getCreatedDateTime());
        endDeviceEvent.setSeverity(createdEndDeviceEvent.getSeverity());
        return endDeviceEvent;
    }

    EndDeviceEvents asEndDeviceEvents(List<HistoricalDeviceAlarm> closedAlarms) {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        closedAlarms.stream().forEach(alarm -> endDeviceEvents.getEndDeviceEvent().addAll(createEndDeviceEvents(alarm)));
        return endDeviceEvents;
    }

    List<EndDeviceEvent> createEndDeviceEvents(HistoricalDeviceAlarm alarm) {
        List<EndDeviceEvent> endDeviceEvents = new ArrayList<>();
        alarm.getDeviceAlarmRelatedEvents().stream()
                .map(DeviceAlarmRelatedEvent::getEventRecord)
                .forEach(eventRecord -> {
                    EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
                    endDeviceEvent.setMRID(eventRecord.getMRID());
                    endDeviceEvent.setCreatedDateTime(eventRecord.getCreatedDateTime());
                    endDeviceEvent.setSeverity(eventRecord.getSeverity());

                    endDeviceEvents.add(endDeviceEvent);
                });
        return endDeviceEvents;
    }
}
