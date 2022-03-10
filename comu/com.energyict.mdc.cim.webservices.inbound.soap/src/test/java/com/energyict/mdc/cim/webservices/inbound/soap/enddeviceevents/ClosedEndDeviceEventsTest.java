/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockEndDeviceEvents;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmRelatedEvent;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsFaultMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.receiveenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class ClosedEndDeviceEventsTest extends AbstractMockEndDeviceEvents {
    @Mock
    private IssueStatus issueStatus;
    @Mock
    private User user;
    @Mock
    private OpenDeviceAlarm openAlarm;
    @Mock
    private HistoricalDeviceAlarm closedAlarm;
    @Mock
    private DeviceAlarmRelatedEvent deviceAlarmRelatedEvent;

    private final String CORRELATION_ID = "CorrelationID";
    private ExecuteEndDeviceEventsEndpoint executeEndDeviceEventsEndpoint;
    private SetMultimap<String, String> values = HashMultimap.create();

    @Before
    public void setUp() throws Exception {
        executeEndDeviceEventsEndpoint = getInstance(ExecuteEndDeviceEventsEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(executeEndDeviceEventsEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1l);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeEndDeviceEventsEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeEndDeviceEventsEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeEndDeviceEventsEndpoint, "transactionService", transactionService);
        when(transactionService.execute(any())).then(new Answer(){
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((ExceptionThrowingSupplier)invocationOnMock.getArguments()[0]).get();
            }
        });
        when(webServicesService.getOngoingOccurrence(1l)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));
        when(meteringService.findEndDeviceByMRID(anyString())).thenReturn(Optional.of(endDevice));
        when(meteringService.getEndDeviceEventType(anyString())).thenReturn(Optional.of(endDeviceEventType));
        when(issueService.findStatus(anyString())).thenReturn(Optional.of(issueStatus));
        when(threadPrincipalService.getPrincipal()).thenReturn(user);

        Finder<OpenDeviceAlarm> finder = mockFinder(Collections.singletonList(openAlarm));
        when(deviceAlarmService.findOpenDeviceAlarms(any(Condition.class))).thenReturn(finder);
        when(openAlarm.addComment(anyString(), any())).thenReturn(Optional.empty());
        when(openAlarm.close(any(IssueStatus.class))).thenReturn(closedAlarm);

        deviceAlarmRelatedEvent = mockDeviceAlarmRelatedEvent();
        when(closedAlarm.getDeviceAlarmRelatedEvents()).thenReturn(Collections.singletonList(deviceAlarmRelatedEvent));
        when(closedAlarm.getDevice()).thenReturn(endDevice);

        mockEndDeviceEvent();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), END_DEVICE_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), END_DEVICE_MRID);
    }

    @Ignore @Test
    public void testCloseEndDeviceEventSuccessfully() throws Exception {
        // Prepare request
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent();
        endDeviceEvent.setReason(END_DEVICE_EVENT_REASON);
        endDeviceEvent.setStatus(createStatus());
        endDeviceEvent.setIssuerID(END_DEVICE_EVENT_ISSUER_ID);
        endDeviceEvent.setIssuerTrackingID(END_DEVICE_EVENT_ISSUER_TRACKING_ID);

        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        EndDeviceEventsEventMessageType endDeviceEventsRequest = createEndDeviceEventsRequest(endDeviceEvents);
        endDeviceEventsRequest.getHeader().setCorrelationID(CORRELATION_ID);
        // Business method
        EndDeviceEventsResponseMessageType response = executeEndDeviceEventsEndpoint.closedEndDeviceEvents(endDeviceEventsRequest);

        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CLOSED);
        assertThat(response.getHeader().getNoun()).isEqualTo("EndDeviceEvents");
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        EndDeviceEvents result = response.getPayload().getEndDeviceEvents();
        assertThat(result.getEndDeviceEvent()).hasSize(1);

        EndDeviceEvent event = result.getEndDeviceEvent().get(0);
        assertThat(event.getMRID()).isEqualTo(END_DEVICE_EVENT_MRID);
        assertThat(event.getCreatedDateTime()).isEqualTo(NOW);
        assertThat(event.getSeverity()).isEqualTo(END_DEVICE_EVENT_SEVERITY);
    }

    @Ignore @Test
    public void testWarningIfMoreThanOneEndDeviceEventSpecified() throws Exception {
        // Prepare request
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent();
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        EndDeviceEventsEventMessageType endDeviceEventsRequest = createEndDeviceEventsRequest(endDeviceEvents);
        endDeviceEventsRequest.getHeader().setCorrelationID(CORRELATION_ID);

        // Business method
        EndDeviceEventsResponseMessageType response = executeEndDeviceEventsEndpoint.closedEndDeviceEvents(endDeviceEventsRequest);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Asserts
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CLOSED);
        assertThat(response.getHeader().getNoun()).isEqualTo("EndDeviceEvents");
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);

        ReplyType reply = response.getReply();
        assertThat(reply.getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertThat(reply.getError()).hasSize(1);
        assertThat(reply.getError().get(0).getCode()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.getErrorCode());
        assertThat(reply.getError().get(0).getLevel()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.getErrorTypeLevel());
        assertThat(reply.getError().get(0).getDetails()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.translate(thesaurus, "EndDeviceEvents.EndDeviceEvent"));
    }

    @Ignore @Test
    public void testCreateDeviceFailedWithLocalizedException() throws Exception {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent();
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        EndDeviceEventsEventMessageType endDeviceEventsRequest = createEndDeviceEventsRequest(endDeviceEvents);
        endDeviceEventsRequest.getHeader().setCorrelationID(CORRELATION_ID);

        LocalizedException localizedException = mock(LocalizedException.class);
        when(localizedException.getLocalizedMessage()).thenReturn("ErrorMessage");
        when(localizedException.getErrorCode()).thenReturn("ERRORCODE");
        when(openAlarm.close(any(IssueStatus.class))).thenThrow(localizedException);

        try {
            // Business method
            executeEndDeviceEventsEndpoint.closedEndDeviceEvents(endDeviceEventsRequest);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);


            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS.translate(thesaurus));
            EndDeviceEventsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo("ERRORCODE");
            assertThat(error.getDetails()).isEqualTo("ErrorMessage");
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Ignore
    @Test
    public void testInvalidSeverity() throws Exception {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = createEndDeviceEvent();
        endDeviceEvent.setSeverity("not alarm");
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);
        EndDeviceEventsEventMessageType endDeviceEventsRequest = createEndDeviceEventsRequest(endDeviceEvents);

        try {
            // Business method
            executeEndDeviceEventsEndpoint.closedEndDeviceEvents(endDeviceEventsRequest);

            verify(webServiceCallOccurrence).saveRelatedAttributes(values);

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
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }
}
