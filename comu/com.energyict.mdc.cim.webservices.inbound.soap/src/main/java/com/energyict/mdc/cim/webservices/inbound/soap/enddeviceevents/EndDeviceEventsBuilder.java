/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.ObjectFactory;
import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import com.google.common.collect.RangeSet;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

public class EndDeviceEventsBuilder {
    private final ObjectFactory payloadObjectFactory = new ObjectFactory();

    private final MeteringService meteringService;
    private final EndDeviceEventsFaultMessageFactory faultMessageFactory;

    private EndDevice endDevice;
    private RangeSet<Instant> timePeriods;

    @Inject
    EndDeviceEventsBuilder(MeteringService meteringService,
                           EndDeviceEventsFaultMessageFactory faultMessageFactory) {
        this.meteringService = meteringService;
        this.faultMessageFactory = faultMessageFactory;
    }

    EndDeviceEventsBuilder fromDeviceWithMRID(String mRID) throws FaultMessage {
        endDevice = meteringService.findEndDeviceByMRID(mRID)
                .orElseThrow(faultMessageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_END_DEVICE_WITH_MRID, mRID));
        return this;
    }

    EndDeviceEventsBuilder fromDeviceWithName(String name) throws FaultMessage {
        endDevice = meteringService.findEndDeviceByName(name)
                .orElseThrow(faultMessageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_END_DEVICE_WITH_NAME, name));
        return this;
    }

    EndDeviceEventsBuilder inTimeIntervals(RangeSet<Instant> timePeriods) {
        this.timePeriods = timePeriods;
        return this;
    }

    EndDeviceEvents build() throws FaultMessage {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        List<EndDeviceEvent> endDeviceEventList = endDeviceEvents.getEndDeviceEvent();

        endDevice.getDeviceEvents(timePeriods.span()).stream().forEach(deviceEvent -> {
            EndDeviceEvent endDeviceEvent = payloadObjectFactory.createEndDeviceEvent();
            endDeviceEvent.setEndDeviceEventType(toEndDeviceEventType(deviceEvent.getEventType()));
            endDeviceEvent.setMRID(deviceEvent.getMRID());
            endDeviceEvent.setCreatedDateTime(deviceEvent.getCreatedDateTime());
            endDeviceEvent.setIssuerID(deviceEvent.getIssuerID());
            endDeviceEvent.setIssuerTrackingID(deviceEvent.getIssuerTrackingID());
            endDeviceEvent.setSeverity(deviceEvent.getSeverity());
            endDeviceEvent.setStatus(toStatus(deviceEvent.getStatus()));

            deviceEvent.getProperties().entrySet().stream().forEach(property -> {
                        EndDeviceEventDetail endDeviceEventDetail = payloadObjectFactory.createEndDeviceEventDetail();
                        endDeviceEventDetail.setName(property.getKey());
                        endDeviceEventDetail.setValue(property.getValue());
                        endDeviceEvent.getEndDeviceEventDetails().add(endDeviceEventDetail);
                    }
            );
            endDeviceEventList.add(endDeviceEvent);
        });
        return endDeviceEvents;
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
