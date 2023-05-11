/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.DateTimeInterval;
import ch.iec.tc57._2011.getenddeviceevents.EndDeviceGroup;
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
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Field;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
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

    private static final String REPLY_ADDRESS = "replyAddress";

    private final ObjectFactory endDeviceEventMessageFactory = new ObjectFactory();
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();

    @Mock
    private EndDevice endDevice;
    @Mock
    private EndDeviceEventType endDeviceEventType;
    @Mock
    private EndDeviceEventRecord endDeviceEvent;
    @Mock
    private ServiceCallCommands serviceCallCommands;
    @Mock
    private WebServiceContext webServiceContext;
    @Mock
    private MessageContext messageContext;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;
    private GetEndDeviceEventsEndpoint getEndDeviceEventsEndpoint;

    @Before
    public void setUp() throws Exception {
        getEndDeviceEventsEndpoint = getInstance(GetEndDeviceEventsEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(getEndDeviceEventsEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1L);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, getEndDeviceEventsEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, getEndDeviceEventsEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, getEndDeviceEventsEndpoint, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
        inject(AbstractInboundEndPoint.class, getEndDeviceEventsEndpoint, "transactionService", transactionService);
        when(webServiceCallOccurrenceService.getOngoingOccurrence(1L)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));
        when(meteringService.findEndDevices(anySetOf(String.class))).thenReturn(Collections.singletonList(endDevice));
        when(endDevice.getMRID()).thenReturn(END_DEVICE_MRID);
        when(endDevice.getName()).thenReturn(END_DEVICE_NAME);
        when(endDevice.getDeviceEvents(any(Range.class))).thenReturn(Collections.singletonList(endDeviceEvent));
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
        when(endDeviceEvent.getEndDevice()).thenReturn(endDevice);
        when(endDeviceEventType.getMRID()).thenReturn(END_DEVICE_EVENT_TYPE);

        HashMap<String, String> eventData = new HashMap<>();
        eventData.put("A", "B");
        when(endDeviceEvent.getProperties()).thenReturn(eventData);
    }

    @Test
    public void testNoMetersInSyncRequest() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);

        try {
            // Business method
            getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));

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
    public void testNoMetersOrGroupsInAsyncRequest() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);
        endDeviceEventsRequest.getHeader().setAsyncReplyFlag(true);
        endDeviceEventsRequest.getHeader().setReplyAddress(REPLY_ADDRESS);
        getEndDeviceEvents.getTimeSchedule().add(createTimeSchedule(NOW, NOW.plusMillis(1000)));

        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration("epc1");
        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS);
        Finder<EndPointConfiguration> finder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);

        try {
            // Business method
            getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
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
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.EMPTY_LIST.translate(thesaurus, "GetEndDeviceEvents.Meters/GetEndDeviceEvents.EndDeviceGroups"));
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testNoMeterOrGroupIdentifiersInRequest() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);
        getEndDeviceEvents.getMeter().add(createMeter("\t\n\r ", null));
        getEndDeviceEvents.getEndDeviceGroup().add(createEndDeviceGroup(null));
        endDeviceEventsRequest.getHeader().setAsyncReplyFlag(true);
        endDeviceEventsRequest.getHeader().setReplyAddress(REPLY_ADDRESS);
        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration("epc1");
        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS);
        Finder<EndPointConfiguration> finder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);

        try {
            // Business method
            getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.DEVICE_OR_GROUP_IDENTIFIER_MISSING.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.DEVICE_OR_GROUP_IDENTIFIER_MISSING.translate(thesaurus));
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
            getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
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
            getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
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
        EndDeviceEventsResponseMessageType response = getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getNoun()).isEqualTo("GetEndDeviceEvents");
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

    @Test
    public void testNoReplyAddress() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);
        endDeviceEventsRequest.getHeader().setAsyncReplyFlag(true);
        getEndDeviceEvents.getMeter().add(createMeter(END_DEVICE_MRID, END_DEVICE_NAME));

        try {
            // Business method
            getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.NO_REPLY_ADDRESS.getErrorCode());
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testSyncModeNotSupported() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);
        endDeviceEventsRequest.getHeader().setAsyncReplyFlag(false);
        Meter meter = createMeter(END_DEVICE_MRID, END_DEVICE_NAME);
        getEndDeviceEvents.getMeter().add(meter);
        getEndDeviceEvents.getMeter().add(meter);

        try {
            // Business method
            getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.SYNC_MODE_NOT_SUPPORTED.getErrorCode());
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testOutboundNotConfigured() throws Exception {
        GetEndDeviceEvents getEndDeviceEvents = new GetEndDeviceEvents();
        GetEndDeviceEventsRequestMessageType endDeviceEventsRequest = createGetEndDeviceEventsRequest(getEndDeviceEvents);
        endDeviceEventsRequest.getHeader().setAsyncReplyFlag(true);
        endDeviceEventsRequest.getHeader().setReplyAddress(REPLY_ADDRESS);

        getEndDeviceEvents.getMeter().add(createMeter(END_DEVICE_MRID, END_DEVICE_NAME));

        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration("epc1");
        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS + "_1");
        Finder<EndPointConfiguration> finder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);

        try {
            // Business method
            getEndDeviceEventsEndpoint.getEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.NO_END_POINT_WITH_URL.getErrorCode());
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    private GetEndDeviceEventsRequestMessageType createGetEndDeviceEventsRequest(GetEndDeviceEvents getEndDeviceEvents) {
        GetEndDeviceEventsRequestType request = endDeviceEventMessageFactory.createGetEndDeviceEventsRequestType();
        request.setGetEndDeviceEvents(getEndDeviceEvents);
        GetEndDeviceEventsRequestMessageType message = endDeviceEventMessageFactory.createGetEndDeviceEventsRequestMessageType();
        message.setRequest(request);
        message.setHeader(cimMessageObjectFactory.createHeaderType());
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

    private EndDeviceGroup createEndDeviceGroup(String name) {
        EndDeviceGroup meterGroup = new EndDeviceGroup();
        Optional.ofNullable(name)
                .map(this::name)
                .ifPresent(meterGroup.getNames()::add);
        return meterGroup;
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
