/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.EndDeviceControlsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.MasterEndDeviceControlsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.SubMasterEndDeviceControlsServiceCallHandler;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;
import com.energyict.mdc.common.device.data.Device;

import ch.iec.tc57._2011.enddevicecontrols.DateTimeInterval;
import ch.iec.tc57._2011.enddevicecontrols.EndDevice;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControl;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControls;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceTiming;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsFaultMessageType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsPayloadType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsRequestMessageType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsResponseMessageType;
import ch.iec.tc57._2011.executeenddevicecontrols.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.SetMultimap;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreatedEndDeviceControlsTest extends AbstractMockActivator {
    private static final String NOUN = "EndDeviceControls";
    private static final String REPLY_ADDRESS = "some_url";
    private static final String CORRELATION_ID = "123456";
    private static final String CIM_CODE = "3.31.0.23";
    private static final String END_DEVICE_MRID = "f86cdede-c8ee-42c8-8c58-dc8f26fe41ad";
    private static final Instant REQUEST_DATE = ZonedDateTime.of(2020, 6, 22, 9, 5, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();
    private static final Instant NOW_DATE = ZonedDateTime.of(2020, 6, 24, 9, 5, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();

    @Mock
    protected WebServiceContext webServiceContext;
    @Mock
    private MessageContext messageContext;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;
    @Mock
    private EndDeviceControlType endDeviceControlType;
    @Mock
    private Device device;
    @Mock
    private Meter endDevice;
    @Mock
    private HeadEndInterface headEndInterface;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CommandFactory commandFactory;
    @Mock
    private ServiceCall masterServiceCall, subServiceCall, deviceServiceCall, deviceServiceCall2;
    @Mock
    private ServiceCallType masterServiceCallType, subServiceCallType, deviceServiceCallType;
    @Mock
    private ServiceCallBuilder serviceCallBuilder, childServiceCallBuilder, subServiceCallBuilder;
    @Mock
    private Finder<ServiceCall> serviceCallFinder;


    private final ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory endDeviceControlsMessageObjectFactory
            = new ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory();
    private final ch.iec.tc57._2011.enddevicecontrols.ObjectFactory endDeviceControlsObjectFactory
            = new ch.iec.tc57._2011.enddevicecontrols.ObjectFactory();
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();

    private ExecuteEndDeviceControlsEndpoint executeEndDeviceControlsEndpoint;

    @Before
    public void setUp() throws Exception {
        executeEndDeviceControlsEndpoint = getInstance(ExecuteEndDeviceControlsEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(executeEndDeviceControlsEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1l);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeEndDeviceControlsEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeEndDeviceControlsEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeEndDeviceControlsEndpoint, "transactionService", transactionService);
        when(transactionService.execute(any())).then(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((ExceptionThrowingSupplier) invocationOnMock.getArguments()[0]).get();
            }
        });
        when(webServicesService.getOngoingOccurrence(1l)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));

        when(endDeviceControlType.getMRID()).thenReturn(CIM_CODE);
        when(meteringService.getEndDeviceControlType(CIM_CODE)).thenReturn(Optional.of(endDeviceControlType));

        when(device.getmRID()).thenReturn(END_DEVICE_MRID);
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(device));
        when(device.getMeter()).thenReturn(endDevice);

        when(headEndInterface.getCommandFactory()).thenReturn(commandFactory);
        when(endDevice.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));

        when(this.clock.instant()).thenReturn(NOW_DATE);

        createServiceCalls();
    }

    @Test
    public void testCreateEndDeviceControlsFaultMessages() throws Exception {
        // No payload
        EndDeviceControlsRequestMessageType requestMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsRequestMessageType();

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Payload' is required");

        //No EndDeviceControls
        EndDeviceControlsPayloadType payloadType = endDeviceControlsMessageObjectFactory.createEndDeviceControlsPayloadType();
        requestMessage.setPayload(payloadType);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Payload.EndDeviceControls' is required");

        //Empty EndDeviceControls
        EndDeviceControls endDeviceControls = endDeviceControlsObjectFactory.createEndDeviceControls();
        payloadType.setEndDeviceControls(endDeviceControls);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.EMPTY_LIST.getErrorCode(),
                "The list of 'CreateEndDeviceControls.Payload.EndDeviceControls' cannot be empty");

        // No header
        EndDeviceControl endDeviceControl = endDeviceControlsObjectFactory.createEndDeviceControl();
        endDeviceControls.getEndDeviceControl().add(endDeviceControl);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Header' is required");

        //Synchronous mode
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(HeaderType.Verb.REPLY);
        requestMessage.setHeader(header);

        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.SYNC_MODE_NOT_SUPPORTED_GENERAL.getErrorCode(),
                "Synchronous mode isn't supported.");

        header.setAsyncReplyFlag(true);

        //No correlation id
        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Header.CorrelationID' is required");

        header.setCorrelationID(CORRELATION_ID);

        //No reply address
        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ELEMENT.getErrorCode(),
                "Element 'CreateEndDeviceControls.Header.ReplyAddress' is required");

        header.setReplyAddress(REPLY_ADDRESS);

        //No endpoint configuration
        assertFaultMessage(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage),
                MessageSeeds.MISSING_ENDPOINT_SEND_END_DEVICE_EVENTS.getErrorCode(),
                "Endpoint for CIM SendEndDeviceEvents isn't found by URL 'some_url'.");

        mockFindEndPointConfigurations();

        //No Cim code
        EndDeviceControl.EndDeviceControlType endDeviceControlType = endDeviceControlsObjectFactory.createEndDeviceControlEndDeviceControlType();
        endDeviceControl.setEndDeviceControlType(endDeviceControlType);

        Map<String, String> codeToMessageMap = new HashMap<>();
        codeToMessageMap.put(MessageSeeds.COMMAND_CODE_MISSING.getErrorCode(), "The command CIM code is missing under EndDeviceControl[0].");
        codeToMessageMap.put(MessageSeeds.RELEASE_DATE_MISSING.getErrorCode(), "The release date is missing under EndDeviceControl[0].");
        codeToMessageMap.put(MessageSeeds.END_DEVICES_MISSING.getErrorCode(), "End devices are missing under EndDeviceControl[0].");

        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage), codeToMessageMap);

        //No release date
        endDeviceControlType.setRef(CIM_CODE);

        codeToMessageMap.remove(MessageSeeds.COMMAND_CODE_MISSING.getErrorCode());
        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage), codeToMessageMap);

        //No End devices
        EndDeviceTiming endDeviceTiming = endDeviceControlsObjectFactory.createEndDeviceTiming();
        DateTimeInterval dateTimeInterval = endDeviceControlsObjectFactory.createDateTimeInterval();
        dateTimeInterval.setStart(REQUEST_DATE);
        endDeviceTiming.setInterval(dateTimeInterval);
        endDeviceControl.setPrimaryDeviceTiming(endDeviceTiming);

        codeToMessageMap.remove(MessageSeeds.RELEASE_DATE_MISSING.getErrorCode());
        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage), codeToMessageMap);

        //No MRID or Name of devices
        EndDevice endDevice = endDeviceControlsObjectFactory.createEndDevice();
        endDeviceControl.getEndDevices().add(endDevice);

        codeToMessageMap.remove(MessageSeeds.END_DEVICES_MISSING.getErrorCode());
        codeToMessageMap.put(MessageSeeds.MISSING_MRID_OR_NAME_FOR_END_DEVICE_CONTROL.getErrorCode(),
                "Either element 'mRID' or 'Names' is required under EndDeviceControl[0].EndDevices[0] for identification purpose.");
        assertFaultMessages(() -> executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage), codeToMessageMap);
    }

    @Test
    public void testCreateEndDeviceControlsSuccessfully() throws Exception {
        when(subServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(deviceServiceCall.getState()).thenReturn(DefaultState.WAITING);

        EndDeviceControlsRequestMessageType requestMessage = createRequest();

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
        verify(commandFactory).createDisconnectCommand(endDevice, null);
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testCreateEndDeviceControlsFailed() throws Exception {
        when(subServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(deviceServiceCall.getState()).thenReturn(DefaultState.FAILED);

        EndDeviceControlsRequestMessageType requestMessage = createRequest();

        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.empty());

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
        assertThat(response.getReply().getError().size()).isEqualTo(1);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM7011")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("For device under EndDeviceControl[0].EndDevices[0]: 'No device found with mrid 'f86cdede-c8ee-42c8-8c58-dc8f26fe41ad''")));

        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    @Test
    public void testCreateEndDeviceControlsPartialSuccessfully() throws Exception {
        when(subServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(deviceServiceCall.getState()).thenReturn(DefaultState.WAITING);
        when(deviceServiceCall2.getState()).thenReturn(DefaultState.FAILED);

        when(subServiceCall.newChildCall(deviceServiceCallType)).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.origin(anyString())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.extendedWith(any())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.targetObject(any())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.create()).thenReturn(deviceServiceCall2);

        when(serviceCallFinder.stream()).then((i) -> Stream.of(deviceServiceCall, deviceServiceCall2));
        when(subServiceCall.findChildren()).thenReturn(serviceCallFinder);

        EndDeviceControlsRequestMessageType requestMessage = createRequest();

        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.empty());

        EndDeviceControlsResponseMessageType response = executeEndDeviceControlsEndpoint.createEndDeviceControls(requestMessage);

        // Assert
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
        assertThat(response.getHeader().getCorrelationID()).isEqualTo(CORRELATION_ID);
        assertThat(response.getHeader().getNoun()).isEqualTo(NOUN);
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertThat(response.getReply().getError().size()).isEqualTo(1);
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getCode().equals("SIM7011")));
        assertTrue(response.getReply().getError().stream()
                .anyMatch(error -> error.getDetails().equals("For device under EndDeviceControl[0].EndDevices[0]: 'No device found with mrid 'f86cdede-c8ee-42c8-8c58-dc8f26fe41ad''")));
        verify(webServiceCallOccurrence).saveRelatedAttributes(any(SetMultimap.class));
    }

    private void createServiceCalls() {
        when(serviceCallService.findServiceCallType(MasterEndDeviceControlsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MasterEndDeviceControlsServiceCallHandler.VERSION))
                .thenReturn(Optional.of(masterServiceCallType));
        when(serviceCallService.findServiceCallType(SubMasterEndDeviceControlsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, SubMasterEndDeviceControlsServiceCallHandler.VERSION))
                .thenReturn(Optional.of(subServiceCallType));
        when(serviceCallService.findServiceCallType(EndDeviceControlsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, EndDeviceControlsServiceCallHandler.VERSION))
                .thenReturn(Optional.of(deviceServiceCallType));

        when(masterServiceCallType.newServiceCall()).thenReturn(serviceCallBuilder);
        when(serviceCallBuilder.origin(anyString())).thenReturn(serviceCallBuilder);
        when(serviceCallBuilder.extendedWith(any())).thenReturn(serviceCallBuilder);
        when(serviceCallBuilder.targetObject(any())).thenReturn(serviceCallBuilder);
        when(serviceCallBuilder.create()).thenReturn(masterServiceCall);

        when(masterServiceCall.newChildCall(subServiceCallType)).thenReturn(subServiceCallBuilder);
        when(subServiceCallBuilder.origin(anyString())).thenReturn(subServiceCallBuilder);
        when(subServiceCallBuilder.extendedWith(any())).thenReturn(subServiceCallBuilder);
        when(subServiceCallBuilder.targetObject(any())).thenReturn(subServiceCallBuilder);
        when(subServiceCallBuilder.create()).thenReturn(subServiceCall);

        when(serviceCallFinder.stream()).then((i) -> Stream.of(subServiceCall));
        when(masterServiceCall.findChildren()).thenReturn(serviceCallFinder);

        when(subServiceCall.newChildCall(deviceServiceCallType)).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.origin(anyString())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.extendedWith(any())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.targetObject(any())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.create()).thenReturn(deviceServiceCall);

        when(serviceCallFinder.stream()).then((i) -> Stream.of(deviceServiceCall));
        when(subServiceCall.findChildren()).thenReturn(serviceCallFinder);
    }

    private EndDeviceControlsRequestMessageType createRequest() {
        EndDeviceControlsRequestMessageType requestMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsRequestMessageType();

        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(HeaderType.Verb.REPLY);
        header.setAsyncReplyFlag(true);
        header.setCorrelationID(CORRELATION_ID);
        header.setReplyAddress(REPLY_ADDRESS);
        requestMessage.setHeader(header);

        EndDeviceControlsPayloadType payloadType = endDeviceControlsMessageObjectFactory.createEndDeviceControlsPayloadType();
        EndDeviceControls endDeviceControls = endDeviceControlsObjectFactory.createEndDeviceControls();

        EndDeviceControl endDeviceControl = endDeviceControlsObjectFactory.createEndDeviceControl();

        EndDeviceControl.EndDeviceControlType endDeviceControlType = endDeviceControlsObjectFactory.createEndDeviceControlEndDeviceControlType();
        endDeviceControlType.setRef(CIM_CODE);
        endDeviceControl.setEndDeviceControlType(endDeviceControlType);

        EndDeviceTiming endDeviceTiming = endDeviceControlsObjectFactory.createEndDeviceTiming();
        DateTimeInterval dateTimeInterval = endDeviceControlsObjectFactory.createDateTimeInterval();
        dateTimeInterval.setStart(REQUEST_DATE);
        endDeviceTiming.setInterval(dateTimeInterval);
        endDeviceControl.setPrimaryDeviceTiming(endDeviceTiming);

        EndDevice endDevice1 = endDeviceControlsObjectFactory.createEndDevice();
        endDevice1.setMRID(END_DEVICE_MRID);

        endDeviceControl.getEndDevices().add(endDevice1);

        endDeviceControls.getEndDeviceControl().add(endDeviceControl);
        payloadType.setEndDeviceControls(endDeviceControls);
        requestMessage.setPayload(payloadType);

        mockFindEndPointConfigurations();

        return requestMessage;
    }

    private void mockFindEndPointConfigurations() {
        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration(REPLY_ADDRESS);
        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(EndDeviceEventsServiceProvider.NAME))
                .thenReturn(Collections.singletonList(endPointConfiguration));
    }

    private void assertFaultMessage(RunnableWithFaultMessage action, String expectedCode, String expectedDetailedMessage) {
        assertFaultMessages(action, Collections.singletonMap(expectedCode, expectedDetailedMessage));
    }

    private void assertFaultMessages(RunnableWithFaultMessage action, Map<String, String> codeToMessageMap) {
        try {
            // Business method
            action.run();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo("Unable to create end device controls.");
            EndDeviceControlsFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            List<ErrorType> errors = faultInfo.getReply().getError();
            assertThat(errors.stream().map(ErrorType::getCode).collect(Collectors.toList()))
                    .containsOnly(codeToMessageMap.keySet().stream().toArray(String[]::new));
            errors.forEach(error -> {
                assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
                assertThat(error.getDetails()).isEqualTo(codeToMessageMap.get(error.getCode()));
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail("Expected FaultMessage but got: " + System.lineSeparator() + e.toString());
        }
    }

    private interface RunnableWithFaultMessage {
        void run() throws FaultMessage;
    }
}
