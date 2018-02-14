/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.getenddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.ObjectFactory;

public class EndDeviceEventsFactory {

    private final ObjectFactory payloadObjectFactory = new ObjectFactory();

    public EndDeviceEvent asEndDeviceEvent(EndDeviceEventRecord record) {
        EndDeviceEvent endDeviceEvent = payloadObjectFactory.createEndDeviceEvent();
        endDeviceEvent.setEndDeviceEventType(toEndDeviceEventType(record.getEventType()));
        endDeviceEvent.setMRID(record.getMRID());
        endDeviceEvent.setCreatedDateTime(record.getCreatedDateTime());
        endDeviceEvent.setIssuerID(record.getIssuerID());
        endDeviceEvent.setIssuerTrackingID(record.getIssuerTrackingID());
        endDeviceEvent.setSeverity(record.getSeverity());
        endDeviceEvent.setStatus(toStatus(record.getStatus()));
        endDeviceEvent.setReason(record.getDescription());

        record.getProperties().entrySet().stream().forEach(property -> {
                    EndDeviceEventDetail endDeviceEventDetail = payloadObjectFactory.createEndDeviceEventDetail();
                    endDeviceEventDetail.setName(property.getKey());
                    endDeviceEventDetail.setValue(property.getValue());
                    endDeviceEvent.getEndDeviceEventDetails().add(endDeviceEventDetail);
                }
        );
        return endDeviceEvent;
    }

    private EndDeviceEvent.EndDeviceEventType toEndDeviceEventType(com.elster.jupiter.metering.events.EndDeviceEventType eventType) {
        EndDeviceEvent.EndDeviceEventType type = payloadObjectFactory.createEndDeviceEventEndDeviceEventType();
        type.setRef(eventType.getMRID());
        return type;
    }

    private ch.iec.tc57._2011.enddeviceevents.Status toStatus(Status status) {
        if (status == null) {
            return null;
        }
        ch.iec.tc57._2011.enddeviceevents.Status state = payloadObjectFactory.createStatus();
        state.setDateTime(status.getDateTime());
        state.setReason(status.getReason());
        state.setRemark(status.getRemark());
        state.setValue(status.getValue());
        return state;
    }
}
