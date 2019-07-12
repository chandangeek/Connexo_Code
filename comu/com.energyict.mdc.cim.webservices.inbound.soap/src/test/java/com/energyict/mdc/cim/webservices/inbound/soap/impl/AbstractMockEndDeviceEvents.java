/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventRecordBuilder;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.Name;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory;
import com.energyict.obis.ObisCode;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractMockEndDeviceEvents extends AbstractMockActivator {
    protected static final Instant NOW = Instant.now();

    protected static final String END_DEVICE_MRID = UUID.randomUUID().toString();
    protected static final String END_DEVICE_NAME = "SPE0000001";

    protected static final String END_DEVICE_EVENT_MRID = UUID.randomUUID().toString();
    protected static final String END_DEVICE_EVENT_ISSUER_ID = "issuerId";
    protected static final String END_DEVICE_EVENT_ISSUER_TRACKING_ID = "issuerTrackingId";
    protected static final String END_DEVICE_EVENT_TYPE = "3.2.22.150";
    protected static final String END_DEVICE_EVENT_STATUS = "open";
    protected static final String END_DEVICE_EVENT_SEVERITY = "alarm";
    protected static final String END_DEVICE_EVENT_REASON ="reason";

    protected final ObjectFactory endDeviceEventMessageFactory = new ObjectFactory();

    @Mock
    protected EndDevice endDevice;
    @Mock
    protected EndDeviceEventType endDeviceEventType;
    @Mock
    protected EndDeviceEventRecord endDeviceEvent;
    @Mock
    protected EndDeviceEventRecordBuilder builder;
    @Mock
    protected EndPointConfiguration endPointConfiguration;
    @Mock
    protected Device device;
    @Mock
    protected LogBook logBook;
    @Mock
    protected WebServiceContext webServiceContext;
    @Mock
    protected MessageContext messageContext;
    @Mock
    protected WebServiceCallOccurrence webServiceCallOccurrence;

    protected EndPointConfiguration mockEndPointConfiguration(String name) {
        EndPointConfiguration mock = mock(EndPointConfiguration.class);
        when(mock.getName()).thenReturn(name);
        when(mock.getWebServiceName()).thenReturn("CIM EndDeviceEvents");

        EndPointProperty propertyMock = mock(EndPointProperty.class);
        when(propertyMock.getName()).thenReturn("endDeviceEvents.obisCode");
        when(propertyMock.getValue()).thenReturn(ObisCode.fromString("1.1.1.1.1.1"));
        when(mock.getProperties()).thenReturn(Collections.singletonList(propertyMock));
        when(endPointConfigurationService.getEndPointConfiguration(name)).thenReturn(Optional.of(mock));
        return mock;
    }

    protected void mockEndDeviceEvent() {
        when(endDeviceEvent.getMRID()).thenReturn(END_DEVICE_EVENT_MRID);
        when(endDeviceEvent.getEventType()).thenReturn(endDeviceEventType);
        when(endDeviceEvent.getCreatedDateTime()).thenReturn(NOW);
        when(endDeviceEvent.getIssuerID()).thenReturn(END_DEVICE_EVENT_ISSUER_ID);
        when(endDeviceEvent.getIssuerTrackingID()).thenReturn(END_DEVICE_EVENT_ISSUER_TRACKING_ID);
        when(endDeviceEvent.getSeverity()).thenReturn(END_DEVICE_EVENT_SEVERITY);
        when(endDeviceEvent.getStatus()).thenReturn(Status.builder().value(END_DEVICE_EVENT_STATUS).build());
        when(endDeviceEvent.getReason()).thenReturn(END_DEVICE_EVENT_REASON);
        when(endDeviceEventType.getMRID()).thenReturn(END_DEVICE_EVENT_TYPE);

        HashMap<String, String> eventData = new HashMap<>();
        eventData.put("A", "B");
        when(endDeviceEvent.getProperties()).thenReturn(eventData);
    }

    protected DeviceAlarmRelatedEvent mockDeviceAlarmRelatedEvent() {
        EndDeviceEventRecord recordMock = mock(EndDeviceEventRecord.class);
        when(recordMock.getMRID()).thenReturn(END_DEVICE_EVENT_MRID);
        when(recordMock.getCreatedDateTime()).thenReturn(NOW);
        when(recordMock.getSeverity()).thenReturn(END_DEVICE_EVENT_SEVERITY);

        DeviceAlarmRelatedEvent eventMock = mock(DeviceAlarmRelatedEvent.class);
        when(eventMock.getEventRecord()).thenReturn(recordMock);

        return eventMock;
    }

    protected EndDeviceEventsEventMessageType createEndDeviceEventsRequest(EndDeviceEvents endDeviceEvents) {
        EndDeviceEventsPayloadType payload = endDeviceEventMessageFactory.createEndDeviceEventsPayloadType();
        payload.setEndDeviceEvents(endDeviceEvents);
        EndDeviceEventsEventMessageType message = endDeviceEventMessageFactory.createEndDeviceEventsEventMessageType();
        message.setPayload(payload);
        return message;
    }

    protected EndDeviceEvent createEndDeviceEvent() {
        EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
        endDeviceEvent.setMRID(END_DEVICE_EVENT_MRID);
        endDeviceEvent.setSeverity(END_DEVICE_EVENT_SEVERITY);
        endDeviceEvent.setCreatedDateTime(NOW);
        endDeviceEvent.setAssets(createAsset(END_DEVICE_MRID, END_DEVICE_NAME));
        EndDeviceEvent.EndDeviceEventType eventType = new EndDeviceEvent.EndDeviceEventType();
        eventType.setRef(END_DEVICE_EVENT_TYPE);
        endDeviceEvent.setEndDeviceEventType(eventType);
        return endDeviceEvent;
    }

    protected Asset createAsset(String mRID, String name) {
        Asset asset = new Asset();
        asset.setMRID(mRID);
        Optional.ofNullable(name)
                .map(this::name)
                .ifPresent(asset.getNames()::add);
        return asset;
    }

    protected Name name(String value) {
        Name name = new Name();
        name.setName(value);
        return name;
    }

    protected ch.iec.tc57._2011.enddeviceevents.Status createStatus() {
        ch.iec.tc57._2011.enddeviceevents.Status status = new ch.iec.tc57._2011.enddeviceevents.Status();
        status.setValue(END_DEVICE_EVENT_STATUS);
        return status;
    }
}
