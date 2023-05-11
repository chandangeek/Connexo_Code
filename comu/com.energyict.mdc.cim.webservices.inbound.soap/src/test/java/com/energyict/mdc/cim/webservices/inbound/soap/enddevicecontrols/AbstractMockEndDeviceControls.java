/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ScheduleStrategy;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.EndDeviceControlsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.EndDeviceControlsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.MasterEndDeviceControlsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.MasterEndDeviceControlsServiceCallHandler;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.SubMasterEndDeviceControlsDomainExtension;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols.SubMasterEndDeviceControlsServiceCallHandler;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.ami.ICommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.DisconnectServiceCallHandler;

import ch.iec.tc57._2011.enddevicecontrols.DateTimeInterval;
import ch.iec.tc57._2011.enddevicecontrols.EndDevice;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControl;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControls;
import ch.iec.tc57._2011.enddevicecontrols.EndDeviceTiming;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsFaultMessageType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsPayloadType;
import ch.iec.tc57._2011.executeenddevicecontrols.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public abstract class AbstractMockEndDeviceControls extends AbstractMockActivator {
    protected static final String NOUN = "EndDeviceControls";
    protected static final String REPLY_ADDRESS = "some_url";
    protected static final String CORRELATION_ID = "123456";
    protected static final String CIM_CODE = "3.31.0.23";
    protected static final String INCORRECT_CIM_CODE = "1.99.99.99";
    protected static final String END_DEVICE_MRID = "f86cdede-c8ee-42c8-8c58-dc8f26fe41ad";
    protected static final String END_DEVICE_MRID_2 = "aaacdede-c8ee-42c8-8c58-dc8f26fe4bbb";
    protected static final Instant PAST_DATE = ZonedDateTime.of(2020, 6, 22, 9, 5, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();
    protected static final Instant NOW_DATE = ZonedDateTime.of(2020, 6, 24, 9, 5, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();
    protected static final Instant FUTURE_DATE = ZonedDateTime.of(2020, 7, 26, 9, 5, 0, 0,
            TimeZoneNeutral.getMcMurdo()).toInstant();

    @Mock
    protected WebServiceContext webServiceContext;
    @Mock
    protected MessageContext messageContext;
    @Mock
    protected WebServiceCallOccurrence webServiceCallOccurrence;
    @Mock
    protected EndDeviceControlType endDeviceControlType;
    @Mock
    protected Device device;
    @Mock
    protected Meter endDevice;
    @Mock
    protected HeadEndInterface headEndInterface;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected CommandFactory commandFactory;
    @Mock
    protected ServiceCall masterServiceCall, subServiceCall, deviceServiceCall, deviceServiceCall2, headEndServiceCall, subServiceCall2;
    @Mock
    protected ServiceCallType masterServiceCallType, subServiceCallType, deviceServiceCallType, headEndServiceCallType;
    @Mock
    protected ServiceCallBuilder serviceCallBuilder, childServiceCallBuilder, subServiceCallBuilder, headEndServiceCallBuilder;
    @Mock
    protected Finder<ServiceCall> masterServiceCallFinder, subServiceCallFinder, endDeviceServiceCallFinder;
    @Mock
    protected MasterEndDeviceControlsDomainExtension masterEndDeviceControlsDomainExtension;
    @Mock
    protected SubMasterEndDeviceControlsDomainExtension subMasterEndDeviceControlsDomainExtension;
    @Mock
    protected EndDeviceControlsDomainExtension endDeviceControlsDomainExtension, endDeviceControlsDomainExtension2;
    @Mock
    protected ICommandServiceCallDomainExtension commandServiceCallDomainExtension;
    @Mock
    protected DeviceMessage deviceMessage;

    protected ExecuteEndDeviceControlsEndpoint executeEndDeviceControlsEndpoint;

    protected final ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory endDeviceControlsMessageObjectFactory
            = new ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory();
    protected final ch.iec.tc57._2011.enddevicecontrols.ObjectFactory endDeviceControlsObjectFactory
            = new ch.iec.tc57._2011.enddevicecontrols.ObjectFactory();
    protected final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();

    abstract String getFaultBasicMessage();

    @Before
    public void setUp() throws Exception {
        executeEndDeviceControlsEndpoint = getInstance(ExecuteEndDeviceControlsEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(executeEndDeviceControlsEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1L);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeEndDeviceControlsEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeEndDeviceControlsEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeEndDeviceControlsEndpoint, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
        inject(AbstractInboundEndPoint.class, executeEndDeviceControlsEndpoint, "transactionService", transactionService);
        when(webServiceCallOccurrenceService.getOngoingOccurrence(1L)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));

        when(ormService.getDataModel(anyString())).thenReturn(Optional.of(dataModel));
        when(this.clock.instant()).thenReturn(NOW_DATE);

        when(endDeviceControlType.getMRID()).thenReturn(CIM_CODE);

        when(device.getmRID()).thenReturn(END_DEVICE_MRID);
        when(device.getId()).thenReturn(1L);
        when(device.getVersion()).thenReturn(1L);
        when(deviceService.findDeviceByMrid(END_DEVICE_MRID)).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceById(1L)).thenReturn(Optional.of(device));
        when(device.getMeter()).thenReturn(endDevice);

        when(headEndInterface.getCommandFactory()).thenReturn(commandFactory);
        when(endDevice.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));

        mockServiceCalls();
    }

    protected void mockServiceCalls() {
        when(serviceCallService.findServiceCallType(MasterEndDeviceControlsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, MasterEndDeviceControlsServiceCallHandler.VERSION))
                .thenReturn(Optional.of(masterServiceCallType));
        when(serviceCallService.findServiceCallType(SubMasterEndDeviceControlsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, SubMasterEndDeviceControlsServiceCallHandler.VERSION))
                .thenReturn(Optional.of(subServiceCallType));
        when(serviceCallService.findServiceCallType(EndDeviceControlsServiceCallHandler.SERVICE_CALL_HANDLER_NAME, EndDeviceControlsServiceCallHandler.VERSION))
                .thenReturn(Optional.of(deviceServiceCallType));
        when(serviceCallService.findServiceCallType(DisconnectServiceCallHandler.SERVICE_CALL_HANDLER_NAME, DisconnectServiceCallHandler.VERSION))
                .thenReturn(Optional.of(headEndServiceCallType));

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

        when(masterServiceCallFinder.stream()).then((i) -> Stream.of(subServiceCall));
        when(masterServiceCall.findChildren()).thenReturn(masterServiceCallFinder);

        when(subServiceCall.newChildCall(deviceServiceCallType)).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.origin(anyString())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.extendedWith(any())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.targetObject(any())).thenReturn(childServiceCallBuilder);
        when(childServiceCallBuilder.create()).thenReturn(deviceServiceCall);

        when(subServiceCallFinder.stream()).then((i) -> Stream.of(deviceServiceCall));
        when(subServiceCall.findChildren()).thenReturn(subServiceCallFinder);

        when(deviceServiceCall.newChildCall(headEndServiceCallType)).thenReturn(headEndServiceCallBuilder);
        when(headEndServiceCallBuilder.origin(anyString())).thenReturn(headEndServiceCallBuilder);
        when(headEndServiceCallBuilder.extendedWith(any())).thenReturn(headEndServiceCallBuilder);
        when(headEndServiceCallBuilder.targetObject(any())).thenReturn(headEndServiceCallBuilder);
        when(headEndServiceCallBuilder.create()).thenReturn(headEndServiceCall);

        when(endDeviceServiceCallFinder.stream()).then((i) -> Stream.of(headEndServiceCall));
        when(deviceServiceCall.findChildren()).thenReturn(endDeviceServiceCallFinder);
        when(endDeviceServiceCallFinder.paged(0, 0)).thenReturn(endDeviceServiceCallFinder);
        when(endDeviceServiceCallFinder.find()).thenReturn(Collections.singletonList(headEndServiceCall));

        QueryStream queryStream = FakeBuilder.initBuilderStub(Optional.of(masterEndDeviceControlsDomainExtension), QueryStream.class);
        when(dataModel.stream(MasterEndDeviceControlsDomainExtension.class)).thenReturn(queryStream);

        when(masterEndDeviceControlsDomainExtension.getServiceCall()).thenReturn(masterServiceCall);
        when(subServiceCall.getExtension(SubMasterEndDeviceControlsDomainExtension.class)).thenReturn(Optional.of(subMasterEndDeviceControlsDomainExtension));
        when(subMasterEndDeviceControlsDomainExtension.getCommandCode()).thenReturn(CIM_CODE);
        when(subMasterEndDeviceControlsDomainExtension.getServiceCall()).thenReturn(subServiceCall);
        when(subMasterEndDeviceControlsDomainExtension.hasRunWithPriorityStrategy()).thenReturn(false);
        when(subMasterEndDeviceControlsDomainExtension.getScheduleStrategy()).thenReturn(ScheduleStrategy.RUN_NOW.getName());
        when(deviceServiceCall.getExtension(EndDeviceControlsDomainExtension.class)).thenReturn(Optional.of(endDeviceControlsDomainExtension));
        when(deviceServiceCall.getParent()).thenReturn(Optional.of(subServiceCall));
        when(endDeviceControlsDomainExtension.getServiceCall()).thenReturn(deviceServiceCall);
        when(endDeviceControlsDomainExtension.getDeviceMrid()).thenReturn(END_DEVICE_MRID);
        when(endDeviceControlsDomainExtension.getTriggerDate()).thenReturn(FUTURE_DATE);
        when(deviceServiceCall2.getExtension(EndDeviceControlsDomainExtension.class)).thenReturn(Optional.of(endDeviceControlsDomainExtension2));
        when(deviceServiceCall2.getParent()).thenReturn(Optional.of(subServiceCall));
        when(endDeviceControlsDomainExtension2.getServiceCall()).thenReturn(deviceServiceCall2);
        when(endDeviceControlsDomainExtension2.getDeviceMrid()).thenReturn(END_DEVICE_MRID_2);
        when(endDeviceControlsDomainExtension2.getTriggerDate()).thenReturn(PAST_DATE);

        doReturn(Optional.of(commandServiceCallDomainExtension)).when(multiSenseHeadEndInterface).getCommandServiceCallDomainExtension(headEndServiceCall);
        doReturn(Optional.of(device)).when(headEndServiceCall).getTargetObject();
        Reference<ServiceCall> refSC = ValueReference.absent();
        refSC.set(headEndServiceCall);
        when(commandServiceCallDomainExtension.getReleaseDate()).thenReturn(FUTURE_DATE);
        when(commandServiceCallDomainExtension.getDeviceMessageIds()).thenReturn(Collections.singletonList(4001L));
        when(deviceMessageService.findAndLockDeviceMessageById(4001L)).thenReturn(Optional.of(deviceMessage));

        when(deviceServiceCall.getId()).thenReturn(1001L);
        when(serviceCallService.lockServiceCall(1001L)).thenReturn(Optional.of(deviceServiceCall));

        when(deviceServiceCall2.getId()).thenReturn(1002L);
        when(serviceCallService.lockServiceCall(1002L)).thenReturn(Optional.of(deviceServiceCall2));

        when(headEndServiceCall.getId()).thenReturn(1003L);
        when(serviceCallService.lockServiceCall(1003L)).thenReturn(Optional.of(headEndServiceCall));
    }

    protected HeaderType createHeader(HeaderType.Verb verb) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        header.setAsyncReplyFlag(true);
        header.setCorrelationID(CORRELATION_ID);
        return header;
    }

    protected EndDeviceControlsPayloadType createPayload(String ref) {
        return createPayload(null, ref);
    }

    protected EndDeviceControlsPayloadType createPayload(Instant releaseDate, String ref) {
        EndDeviceControlsPayloadType payloadType = endDeviceControlsMessageObjectFactory.createEndDeviceControlsPayloadType();
        EndDeviceControls endDeviceControls = endDeviceControlsObjectFactory.createEndDeviceControls();

        EndDeviceControl endDeviceControl = endDeviceControlsObjectFactory.createEndDeviceControl();

        EndDeviceControl.EndDeviceControlType endDeviceControlType = endDeviceControlsObjectFactory.createEndDeviceControlEndDeviceControlType();
        endDeviceControlType.setRef(ref);
        endDeviceControl.setEndDeviceControlType(endDeviceControlType);

        if (releaseDate != null) {
            EndDeviceTiming endDeviceTiming = endDeviceControlsObjectFactory.createEndDeviceTiming();
            DateTimeInterval dateTimeInterval = endDeviceControlsObjectFactory.createDateTimeInterval();
            dateTimeInterval.setStart(releaseDate);
            endDeviceTiming.setInterval(dateTimeInterval);
            endDeviceControl.setPrimaryDeviceTiming(endDeviceTiming);
        }

        EndDevice endDevice1 = endDeviceControlsObjectFactory.createEndDevice();
        endDevice1.setMRID(END_DEVICE_MRID);

        endDeviceControl.getEndDevices().add(endDevice1);

        endDeviceControls.getEndDeviceControl().add(endDeviceControl);
        payloadType.setEndDeviceControls(endDeviceControls);
        return payloadType;
    }

    protected void mockFindEndPointConfigurations() {
        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration(REPLY_ADDRESS);
        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(EndDeviceEventsServiceProvider.NAME))
                .thenReturn(Collections.singletonList(endPointConfiguration));
    }

    protected void assertFaultMessage(RunnableWithFaultMessage action, String expectedCode, String expectedDetailedMessage) {
        assertFaultMessages(action, Collections.singletonMap(expectedCode, expectedDetailedMessage));
    }

    protected void assertFaultMessages(RunnableWithFaultMessage action, Map<String, String> codeToMessageMap) {
        try {
            // Business method
            action.run();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(getFaultBasicMessage());
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

    interface RunnableWithFaultMessage {
        void run() throws FaultMessage;
    }
}
