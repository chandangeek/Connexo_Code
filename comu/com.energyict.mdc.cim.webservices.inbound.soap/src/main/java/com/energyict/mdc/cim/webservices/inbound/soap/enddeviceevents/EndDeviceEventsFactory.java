/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;

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
}
