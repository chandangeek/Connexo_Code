/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.metering.EndDevice;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;

import java.util.ArrayList;
import java.util.List;

class EndDeviceEventsFactory {
    EndDeviceEvents asEndDeviceEvents(com.elster.jupiter.metering.readings.EndDeviceEvent endDeviceEvent, EndDevice endDevice) {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        endDeviceEvents.getEndDeviceEvent().add(createEndDeviceEvent(endDeviceEvent, endDevice));
        return endDeviceEvents;
    }

    EndDeviceEvent createEndDeviceEvent(com.elster.jupiter.metering.readings.EndDeviceEvent endDeviceEvent, EndDevice endDevice) {
        EndDeviceEvent event = new EndDeviceEvent();
        event.setMRID(endDeviceEvent.getMRID());
        event.setCreatedDateTime(endDeviceEvent.getCreatedDateTime());
        event.setSeverity(endDeviceEvent.getSeverity());
        event.setAssets(createAsset(endDevice));
        return event;
    }

    Asset createAsset(EndDevice endDevice) {
        Asset asset = new Asset();
        asset.setMRID(endDevice.getMRID());
        return asset;
    }

    EndDeviceEvents asEndDeviceEvents(List<HistoricalDeviceAlarm> closedAlarms) {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        closedAlarms.stream().forEach(alarm -> endDeviceEvents.getEndDeviceEvent().addAll(createEndDeviceEvents(alarm)));
        return endDeviceEvents;
    }

    List<EndDeviceEvent> createEndDeviceEvents(HistoricalDeviceAlarm alarm) {
        List<EndDeviceEvent> endDeviceEvents = new ArrayList<>();
        Asset asset = createAsset(alarm.getDevice());
        alarm.getDeviceAlarmRelatedEvents().stream()
                .map(DeviceAlarmRelatedEvent::getEventRecord)
                .forEach(eventRecord -> {
                    EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
                    endDeviceEvent.setMRID(eventRecord.getMRID());
                    endDeviceEvent.setCreatedDateTime(eventRecord.getCreatedDateTime());
                    endDeviceEvent.setSeverity(eventRecord.getSeverity());
                    endDeviceEvent.setAssets(asset);
                    endDeviceEvents.add(endDeviceEvent);
                });
        return endDeviceEvents;
    }
}
