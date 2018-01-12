/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventRecordBuilder;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.Name;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsFaultMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory;
import ch.iec.tc57._2011.receiveenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.energyict.obis.ObisCode;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CreatedEndDeviceEventsTest extends AbstractMockActivator {

    private static final Instant NOW = Instant.now();

    private static final String END_DEVICE_MRID = UUID.randomUUID().toString();
    private static final String END_DEVICE_NAME = "SPE0000001";

    private static final String END_DEVICE_EVENT_MRID = UUID.randomUUID().toString();
    private static final String END_DEVICE_EVENT_ISSUER_ID = "issuerId";
    private static final String END_DEVICE_EVENT_ISSUER_TRACKING_ID = "issuerTrackingId";
    private static final String END_DEVICE_EVENT_TYPE = "3.2.22.150";
    private static final String END_DEVICE_EVENT_STATUS = "open";
    private static final String END_DEVICE_EVENT_SEVERITY = "alarm";
    private static final String END_DEVICE_EVENT_REASON ="reason";

    private final ObjectFactory endDeviceEventMessageFactory = new ObjectFactory();

    @Mock
    private EndDevice endDevice;
    @Mock
    private EndDeviceEventType endDeviceEventType;
    @Mock
    private EndDeviceEventRecord endDeviceEvent;
    @Mock
    private EndDeviceEventRecordBuilder builder;
    @Mock
    private EndPointConfiguration endPointConfiguration;
    @Mock
    private Device device;
    @Mock
    private LogBook logBook;

    @Before
    public void setUp() throws Exception {
        when(meteringService.findEndDeviceByMRID(anyString())).thenReturn(Optional.of(endDevice));
        when(meteringService.getEndDeviceEventType(anyString())).thenReturn(Optional.of(endDeviceEventType));

        when(deviceService.findDeviceById(any(Integer.class))).thenReturn(Optional.of(device));

        endPointConfiguration = mockEndPointConfiguration("epc1");
        Finder<EndPointConfiguration> finder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);

        when(builder.create()).thenReturn(endDeviceEvent);

        when(endDevice.getAmrId()).thenReturn("1");
        when(endDevice.addEventRecord(any(EndDeviceEventType.class), any(Instant.class))).thenReturn(builder);

        when(logBookService.findByDeviceAndObisCode(any(Device.class), any(ObisCode.class))).thenReturn(Optional.of(logBook));

        mockEndDeviceEvent();
    }

    private EndPointConfiguration mockEndPointConfiguration(String name) {
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

    private void mockEndDeviceEvent() {
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

    @Test
    public void testCreateEndDeviceEventSuccessfully() throws Exception {
        // Prepare request
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent();
        endDeviceEvent.setReason(END_DEVICE_EVENT_REASON);
        endDeviceEvent.setStatus(createStatus());
        endDeviceEvent.setIssuerID(END_DEVICE_EVENT_ISSUER_ID);
        endDeviceEvent.setIssuerTrackingID(END_DEVICE_EVENT_ISSUER_TRACKING_ID);

        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        EndDeviceEventsEventMessageType endDeviceEventsRequest = createEndDeviceEventsRequest(endDeviceEvents);

        // Business method
        EndDeviceEventsResponseMessageType response = getInstance(ExecuteEndDeviceEventsEndpoint.class).createdEndDeviceEvents(endDeviceEventsRequest);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
        assertThat(response.getHeader().getNoun()).isEqualTo("EndDeviceEvents");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        EndDeviceEvents result = response.getPayload().getEndDeviceEvents();
        assertThat(result.getEndDeviceEvent()).hasSize(1);

        EndDeviceEvent event = result.getEndDeviceEvent().get(0);
        assertThat(event.getMRID()).isEqualTo(END_DEVICE_EVENT_MRID);
        assertThat(event.getCreatedDateTime()).isEqualTo(NOW);
        assertThat(event.getSeverity()).isEqualTo(END_DEVICE_EVENT_SEVERITY);
    }

    @Test
    public void testWarningIfMoreThanOneEndDeviceEventSpecified() throws Exception {
        // Prepare request
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent();
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        EndDeviceEventsEventMessageType endDeviceEventsRequest = createEndDeviceEventsRequest(endDeviceEvents);

        // Business method
        EndDeviceEventsResponseMessageType response = getInstance(ExecuteEndDeviceEventsEndpoint.class).createdEndDeviceEvents(endDeviceEventsRequest);

        // Asserts
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
        assertThat(response.getHeader().getNoun()).isEqualTo("EndDeviceEvents");
        ReplyType reply = response.getReply();
        assertThat(reply.getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertThat(reply.getError()).hasSize(1);
        assertThat(reply.getError().get(0).getCode()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.getErrorCode());
        assertThat(reply.getError().get(0).getLevel()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.getErrorTypeLevel());
        assertThat(reply.getError().get(0).getDetails()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.translate(thesaurus, "EndDeviceEvents.EndDeviceEvent"));
    }

    @Test
    public void testCreateDeviceFailedWithLocalizedException() throws Exception {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent();
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        EndDeviceEventsEventMessageType endDeviceEventsRequest = createEndDeviceEventsRequest(endDeviceEvents);

        LocalizedException localizedException = mock(LocalizedException.class);
        when(localizedException.getLocalizedMessage()).thenReturn("ErrorMessage");
        when(localizedException.getErrorCode()).thenReturn("ERRORCODE");
        when(builder.create()).thenThrow(localizedException);

        try {
            // Business method
            getInstance(ExecuteEndDeviceEventsEndpoint.class).createdEndDeviceEvents(endDeviceEventsRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo("ERRORCODE");
            assertThat(error.getDetails()).isEqualTo("ErrorMessage");

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testInvalidSeverity() throws Exception {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent();
        endDeviceEvent.setSeverity("not alarm");
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        EndDeviceEventsEventMessageType endDeviceEventsRequest = createEndDeviceEventsRequest(endDeviceEvents);

        try {
            // Business method
            getInstance(ExecuteEndDeviceEventsEndpoint.class).createdEndDeviceEvents(endDeviceEventsRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.INVALID_SEVERITY.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.INVALID_SEVERITY.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.INVALID_SEVERITY.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.INVALID_SEVERITY.translate(thesaurus));

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    private EndDeviceEventsEventMessageType createEndDeviceEventsRequest(EndDeviceEvents endDeviceEvents) {
        EndDeviceEventsPayloadType payload = endDeviceEventMessageFactory.createEndDeviceEventsPayloadType();
        payload.setEndDeviceEvents(endDeviceEvents);
        EndDeviceEventsEventMessageType message = endDeviceEventMessageFactory.createEndDeviceEventsEventMessageType();
        message.setPayload(payload);
        return message;
    }

    private EndDeviceEvent createEndDeviceEvent() {
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

    private Asset createAsset(String mRID, String name) {
        Asset asset = new Asset();
        asset.setMRID(mRID);
        Optional.ofNullable(name)
                .map(this::name)
                .ifPresent(asset.getNames()::add);
        return asset;
    }

    private Name name(String value) {
        Name name = new Name();
        name.setName(value);
        return name;
    }

    private ch.iec.tc57._2011.enddeviceevents.Status createStatus() {
        ch.iec.tc57._2011.enddeviceevents.Status status = new ch.iec.tc57._2011.enddeviceevents.Status();
        status.setValue(END_DEVICE_EVENT_STATUS);
        return status;
    }
}
