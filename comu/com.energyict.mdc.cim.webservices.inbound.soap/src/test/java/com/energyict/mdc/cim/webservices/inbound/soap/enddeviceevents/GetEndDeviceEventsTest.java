/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.DateTimeInterval;
import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.Meter;
import ch.iec.tc57._2011.getenddeviceevents.Name;
import ch.iec.tc57._2011.getenddeviceevents.TimeSchedule;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsFaultMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.GetEndDeviceEventsRequestMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.GetEndDeviceEventsRequestType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.RangeSet;

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
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.when;

public class GetEndDeviceEventsTest extends AbstractMockActivator {

    private static final Instant NOW = Instant.now();

    private static final String END_DEVICE_MRID = UUID.randomUUID().toString();
    private static final String END_DEVICE_NAME = "SPE0000001";

    private static final String END_DEVICE_EVENT_MRID = UUID.randomUUID().toString();
    private static final String END_DEVICE_EVENT_ISSUER_ID = "issuerId";
    private static final String END_DEVICE_EVENT_ISSUER_TRACKING_ID = "issuerTrackingId";
    private static final String END_DEVICE_EVENT_SEVERIRY = "medium";
    private static final String END_DEVICE_EVENT_TYPE = "3.2.22.150";
    private static final String END_DEVICE_EVENT_STATUS = "open";

    private final ObjectFactory endDeviceEventMessageFactory = new ObjectFactory();

    @Mock
    private EndDevice endDevice;
    @Mock
    private EndDeviceEventType endDeviceEventType;
    @Mock
    private EndDeviceEventRecord endDeviceEvent;

    @Before
    public void setUp() throws Exception {
        Finder<EndDevice> finder = mockFinder(Collections.singletonList(endDevice));
        when(meteringService.findEndDevices(anySetOf(String.class), anySetOf(String.class))).thenReturn(finder);
        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(endDevice.getName()).thenReturn(END_DEVICE_NAME);
        when(endDevice.getDeviceEvents(any(RangeSet.class))).thenReturn(Collections.singletonList(endDeviceEvent));
        mockEndDeviceEvent();
    }

    private void mockEndDeviceEvent() {
        when(endDeviceEvent.getMRID()).thenReturn(END_DEVICE_EVENT_MRID);
        when(endDeviceEvent.getEventType()).thenReturn(endDeviceEventType);
        when(endDeviceEvent.getCreatedDateTime()).thenReturn(NOW);
        when(endDeviceEvent.getIssuerID()).thenReturn(END_DEVICE_EVENT_ISSUER_ID);
        when(endDeviceEvent.getIssuerTrackingID()).thenReturn(END_DEVICE_EVENT_ISSUER_TRACKING_ID);
        when(endDeviceEvent.getSeverity()).thenReturn(END_DEVICE_EVENT_SEVERIRY);
        when(endDeviceEvent.getStatus()).thenReturn(Status.builder().value(END_DEVICE_EVENT_STATUS).build());
        when(endDeviceEventType.getMRID()).thenReturn(END_DEVICE_EVENT_TYPE);

        HashMap<String, String> eventData = new HashMap<>();
        eventData.put("A", "B");
        when(endDeviceEvent.getProperties()).thenReturn(eventData);
    }

    @Test
    public void testNoMetersInRequest() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);

        try {
            // Business method
            getInstance(GetEndDeviceEventsEndpoint.class).getEndDeviceEvents(endDeviceEventsRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.EMPTY_LIST.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.EMPTY_LIST.translate(thesaurus, "GetEndDeviceEvents.Meters"));
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testNoMeterIdentifiersInRequest() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);
        getEndDeviceEvents.getMeter().add(createMeter("\t\n\r ", null));

        try {
            // Business method
            getInstance(GetEndDeviceEventsEndpoint.class).getEndDeviceEvents(endDeviceEventsRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.END_DEVICE_IDENTIFIER_MISSING.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.END_DEVICE_IDENTIFIER_MISSING.translate(thesaurus));
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testMissingStartPeriodInRequest() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);

        DateTimeInterval dateTimeInterval = new DateTimeInterval();
        dateTimeInterval.setEnd(NOW);

        TimeSchedule timeSchedule = new TimeSchedule();
        timeSchedule.setScheduleInterval(dateTimeInterval);

        getEndDeviceEvents.getTimeSchedule().add(timeSchedule);

        getEndDeviceEvents.getMeter().add(createMeter(END_DEVICE_MRID, END_DEVICE_NAME));

        try {
            // Business method
            getInstance(GetEndDeviceEventsEndpoint.class).getEndDeviceEvents(endDeviceEventsRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.MISSING_ELEMENT.translate(thesaurus, "TimeSchedule.start"));
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testInvalidPeriodInRequest() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);

        DateTimeInterval dateTimeInterval = new DateTimeInterval();
        dateTimeInterval.setEnd(NOW);
        dateTimeInterval.setStart(NOW.plusMillis(1000));

        TimeSchedule timeSchedule = new TimeSchedule();
        timeSchedule.setScheduleInterval(dateTimeInterval);

        getEndDeviceEvents.getTimeSchedule().add(timeSchedule);

        getEndDeviceEvents.getMeter().add(createMeter(END_DEVICE_MRID, END_DEVICE_NAME));

        try {
            // Business method
            getInstance(GetEndDeviceEventsEndpoint.class).getEndDeviceEvents(endDeviceEventsRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD.translate(thesaurus,
                    XsdDateTimeConverter.marshalDateTime(dateTimeInterval.getStart()),
                    XsdDateTimeConverter.marshalDateTime(dateTimeInterval.getEnd())));
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testSuccessfulGet() throws Exception {
        // Prepare request
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);
        getEndDeviceEvents.getMeter().add(createMeter(END_DEVICE_MRID, END_DEVICE_NAME));
        getEndDeviceEvents.getTimeSchedule().add(createTimeSchedule(NOW.minusMillis(1000), NOW.plusMillis(1000)));

        // Business method
        EndDeviceEventsResponseMessageType response = getInstance(GetEndDeviceEventsEndpoint.class).getEndDeviceEvents(endDeviceEventsRequest);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("EndDeviceEvents");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        EndDeviceEvents endDeviceEvents = response.getPayload().getEndDeviceEvents();
        assertThat(endDeviceEvents.getEndDeviceEvent()).hasSize(1);

        ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent endDeviceEvent = endDeviceEvents.getEndDeviceEvent().get(0);
        assertThat(endDeviceEvent.getMRID()).isEqualTo(END_DEVICE_EVENT_MRID);
        assertThat(endDeviceEvent.getCreatedDateTime()).isEqualTo(NOW);
        assertThat(endDeviceEvent.getIssuerID()).isEqualTo(END_DEVICE_EVENT_ISSUER_ID);
        assertThat(endDeviceEvent.getIssuerTrackingID()).isEqualTo(END_DEVICE_EVENT_ISSUER_TRACKING_ID);
        assertThat(endDeviceEvent.getSeverity()).isEqualTo(END_DEVICE_EVENT_SEVERIRY);
        assertThat(endDeviceEvent.getEndDeviceEventType().getRef()).isEqualTo(END_DEVICE_EVENT_TYPE);
        assertThat(endDeviceEvent.getStatus().getValue()).isEqualTo(END_DEVICE_EVENT_STATUS);

        assertThat(endDeviceEvent.getEndDeviceEventDetails()).hasSize(1);
        assertThat(endDeviceEvent.getEndDeviceEventDetails().get(0).getName()).isEqualTo("A");
        assertThat(endDeviceEvent.getEndDeviceEventDetails().get(0).getValue()).isEqualTo("B");
    }

    private GetEndDeviceEventsRequestMessageType createGetEndDeviceEventsRequest(GetEndDeviceEvents getEndDeviceEvents) {
        GetEndDeviceEventsRequestType request = endDeviceEventMessageFactory.createGetEndDeviceEventsRequestType();
        request.setGetEndDeviceEvents(getEndDeviceEvents);
        GetEndDeviceEventsRequestMessageType message = endDeviceEventMessageFactory.createGetEndDeviceEventsRequestMessageType();
        message.setRequest(request);
        return message;
    }

    private Meter createMeter(String mRID, String name) {
        Meter meter = new Meter();
        meter.setMRID(mRID);
        Optional.ofNullable(name)
                .map(this::name)
                .ifPresent(meter.getNames()::add);
        return meter;
    }

    private Name name(String value) {
        Name name = new Name();
        name.setName(value);
        return name;
    }

    private TimeSchedule createTimeSchedule(Instant start, Instant end) {
        DateTimeInterval dateTimeInterval = new DateTimeInterval();
        dateTimeInterval.setStart(start);
        dateTimeInterval.setEnd(end);
        TimeSchedule timeSchedule = new TimeSchedule();
        timeSchedule.setScheduleInterval(dateTimeInterval);
        return timeSchedule;
    }

}
