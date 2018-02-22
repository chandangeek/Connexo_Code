/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.getenddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.Name;
import ch.iec.tc57._2011.enddeviceevents.NameType;
import ch.iec.tc57._2011.enddeviceevents.ObjectFactory;

public class EndDeviceEventsFactory {
    private static final String END_DEVICE_NAME_TYPE = "EndDevice";

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
        endDeviceEvent.setAssets(createAsset(record.getEndDevice()));

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

    private Asset createAsset(EndDevice endDevice) {
        Asset asset = payloadObjectFactory.createAsset();
        asset.setMRID(endDevice.getMRID());
        asset.getNames().add(createName(endDevice));
        return asset;
    }

    private Name createName(EndDevice endDevice) {
        NameType nameType = payloadObjectFactory.createNameType();
        nameType.setName(END_DEVICE_NAME_TYPE);
        Name name = payloadObjectFactory.createName();
        name.setNameType(nameType);
        name.setName(endDevice.getName());
        return name;
    }
}
