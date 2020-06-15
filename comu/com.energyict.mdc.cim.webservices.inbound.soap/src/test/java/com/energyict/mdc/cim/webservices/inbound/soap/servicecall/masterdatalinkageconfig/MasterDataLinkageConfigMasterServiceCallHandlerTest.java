/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.ObjectHolder;
import com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageAction;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;
import com.energyict.mdc.cim.webservices.outbound.soap.FailedLinkageOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.LinkageOperation;
import com.energyict.mdc.cim.webservices.outbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.json.JsonService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MasterDataLinkageConfigMasterServiceCallHandlerTest {

    private static final String CALLBACK_URL = "my callback url";

    private static final MasterDataLinkageAction OPERATION = MasterDataLinkageAction.CREATE;

    private static final BigDecimal NUMBER_OF_SUCCESSFUL_CALLS = BigDecimal.valueOf(2);
    private static final BigDecimal NUMBER_OF_FAILED_CALLS = BigDecimal.valueOf(4);

    private static String METER_MRID = "my meter mrid";
    private static String METER_NAME = "my meter name";
    private static String USAGE_POINT_MRID = "my usage point mrid";
    private static String USAGE_POINT_NAME = "my usage point name";

    private static final String ERROR_CODE = "my error code";

    private static final String ERROR_MESSAGE = "my error message";

    private static final String CORRELARION_ID = "CorrelationID";

    private MasterDataLinkageConfigMasterServiceCallHandler handler;

    private ObjectHolder<ReplyMasterDataLinkageConfigWebService> replyMasterDataLinkageConfigWebServiceHolder;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private ReplyMasterDataLinkageConfigWebService replyMasterDataLinkageConfigWebService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceCall serviceCall;
    @Mock
    private Finder<ServiceCall> finder;
    @Mock
    private EndPointConfiguration endPointConfiguration;
    @Mock
    private MasterDataLinkageConfigMasterDomainExtension masterExtension;
    @Mock
    private ServiceCall childServiceCallSuccess;
    @Mock
    private ServiceCall childServiceCallFailure;
    @Mock
    private JsonService jsonService;
    @Mock
    private UsagePointInfo usagePointInfo;
    @Mock
    private MeterInfo meterInfo;
    @Mock
    private MasterDataLinkageConfigDomainExtension masterDataLinkageConfigDomainExtension;
    @Mock
    private MeteringService meterService;
    @Mock
    private Meter meter;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private WebServicesService webServicesService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Thesaurus thesaurus;

    @Before
    public void setup() {
        replyMasterDataLinkageConfigWebServiceHolder = new ObjectHolder<>();
        replyMasterDataLinkageConfigWebServiceHolder.setObject(replyMasterDataLinkageConfigWebService);
        handler = new MasterDataLinkageConfigMasterServiceCallHandler(endPointConfigurationService,
                replyMasterDataLinkageConfigWebServiceHolder, jsonService, meterService, thesaurus, webServicesService);

        when(endPointConfigurationService.findEndPointConfigurations().find().stream())
                .thenReturn(Stream.of(endPointConfiguration));
        when(endPointConfiguration.isActive()).thenReturn(true);
        when(endPointConfiguration.isInbound()).thenReturn(false);
        when(endPointConfiguration.getUrl()).thenReturn(CALLBACK_URL);

        when(serviceCall.getExtension(MasterDataLinkageConfigMasterDomainExtension.class))
                .thenReturn(Optional.of(masterExtension));
        when(masterExtension.getCallbackURL()).thenReturn(CALLBACK_URL);
        when(masterExtension.getActualNumberOfSuccessfulCalls()).thenReturn(NUMBER_OF_SUCCESSFUL_CALLS);
        when(masterExtension.getActualNumberOfFailedCalls()).thenReturn(NUMBER_OF_FAILED_CALLS);
        when(masterExtension.getExpectedNumberOfCalls()).thenReturn(
                BigDecimal.valueOf(NUMBER_OF_SUCCESSFUL_CALLS.intValue() + NUMBER_OF_FAILED_CALLS.intValue() + 1));

        when(masterExtension.getCorrelationId()).thenReturn(CORRELARION_ID);
        when(serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)).thenReturn(true);
        when(serviceCall.canTransitionTo(DefaultState.FAILED)).thenReturn(true);
        when(serviceCall.canTransitionTo(DefaultState.PARTIAL_SUCCESS)).thenReturn(true);

        when(jsonService.deserialize(anyString(), eq(UsagePointInfo.class))).thenReturn(usagePointInfo);
        when(jsonService.deserialize(anyString(), eq(MeterInfo.class))).thenReturn(meterInfo);

        when(serviceCall.findChildren()).thenReturn(finder);
        // we cannot use thenReturn because the call happens more than once and the stream is closed after first call
        doAnswer((Answer<Stream<ServiceCall>>) invocation -> Stream.of(childServiceCallSuccess, childServiceCallFailure)).when(finder).stream();

        when(childServiceCallSuccess.getExtension(MasterDataLinkageConfigDomainExtension.class))
                .thenReturn(Optional.of(masterDataLinkageConfigDomainExtension));
        when(childServiceCallSuccess.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCallFailure.getExtension(MasterDataLinkageConfigDomainExtension.class))
                .thenReturn(Optional.of(masterDataLinkageConfigDomainExtension));
        when(childServiceCallFailure.getState()).thenReturn(DefaultState.FAILED);

        when(masterDataLinkageConfigDomainExtension.getOperation()).thenReturn(OPERATION.name());
        when(masterDataLinkageConfigDomainExtension.getErrorCode()).thenReturn(ERROR_CODE);
        when(masterDataLinkageConfigDomainExtension.getErrorMessage()).thenReturn(ERROR_MESSAGE);

        when(meterService.findMeterByName(anyString())).thenReturn(Optional.of(meter));
        when(meter.getMRID()).thenReturn(METER_MRID);
        when(meter.getName()).thenReturn(METER_NAME);

        when(meterService.findUsagePointByName(anyString())).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMRID()).thenReturn(USAGE_POINT_MRID);
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);

        when(webServicesService.isPublished(any(EndPointConfiguration.class))).thenReturn(true);

    }

    @Test
    public void testTransitionToPending() {
        handler.onStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING);

        verify(serviceCall).requestTransition(DefaultState.ONGOING);
    }

    @Test
    public void testTransitionToOnGoing() {
        handler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        verify(childServiceCallSuccess).requestTransition(DefaultState.PENDING);
        verify(childServiceCallFailure).requestTransition(DefaultState.PENDING);
        verify(serviceCall).findChildren();
        verifyNoMoreInteractions(serviceCall, childServiceCallSuccess, childServiceCallFailure);
    }

    @Test
    public void testTransitionToFailedShouldNotSendResponseWhenThereIsNoReplyWebServiceImplementation() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoReplyWebServiceImplementation(DefaultState.FAILED);
    }

    @Test
    public void testTransitionToSuccessfulShouldNotSendResponseWhenThereIsNoReplyWebServiceImplementation() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoReplyWebServiceImplementation(
                DefaultState.SUCCESSFUL);
    }

    @Test
    public void testTransitionToPartialSuccessShouldNotSendResponseWhenThereIsNoReplyWebServiceImplementation() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoReplyWebServiceImplementation(
                DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void testTransitionToFailedShouldNotSendResponseWhenThereIsNoActiveEndpoint() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoActiveEndpoint(DefaultState.FAILED);
    }

    @Test
    public void testTransitionToSuccessfulShouldNotSendResponseWhenThereIsNoActiveEndpoint() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoActiveEndpoint(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testTransitionToPartialSuccessShouldNotSendResponseWhenThereIsNoActiveEndpoint() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoActiveEndpoint(DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void testTransitionToFailedShouldNotSendResponseWhenThereIsNoOutboundEndpoint() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoOutboundEndpoint(DefaultState.FAILED);
    }

    @Test
    public void testTransitionToSuccessfulShouldNotSendResponseWhenThereIsNoOutboundEndpoint() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoOutboundEndpoint(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testTransitionToPartialSuccessShouldNotSendResponseWhenThereIsNoOutboundEndpoint() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoOutboundEndpoint(DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void testTransitionToFailedShouldNotSendResponseWhenThereIsNoEndpointWithProvidedCallbackUrl() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoEndpointWithProvidedCallbackUrl(
                DefaultState.FAILED);
    }

    @Test
    public void testTransitionToSuccessfulShouldNotSendResponseWhenThereIsNoEndpointWithProvidedCallbackUrl() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoEndpointWithProvidedCallbackUrl(
                DefaultState.SUCCESSFUL);
    }

    @Test
    public void testTransitionToPartialSuccessShouldNotSendResponseWhenThereIsNoEndpointWithProvidedCallbackUrl() {
        doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoEndpointWithProvidedCallbackUrl(
                DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void testTransitionToFailedShouldSendResponse() {
        doTestTransitionToEndStateShouldSendResponse(DefaultState.FAILED);
    }

    @Test
    public void testTransitionToFailedShouldSendResponseNoMeterFound() {
        when(meterService.findMeterByName(anyString())).thenReturn(Optional.empty());
        when(meter.getMRID()).thenReturn(null);
        when(meter.getName()).thenReturn(null);
        METER_MRID = null;
        METER_NAME = null;
        doTestTransitionToEndStateShouldSendResponse(DefaultState.FAILED);
    }

    @Test
    public void testTransitionToFailedShouldSendResponseNoUsagePointFound(){
        when(meterService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(usagePoint.getMRID()).thenReturn(null);
        when(usagePoint.getName()).thenReturn(null);
        USAGE_POINT_MRID = null;
        USAGE_POINT_NAME = null;
        doTestTransitionToEndStateShouldSendResponse(DefaultState.FAILED);
    }

    @Test
    public void testTransitionToFailedShouldSendResponseNoUsagePointAndMeterFound(){
        when(meterService.findMeterByName(anyString())).thenReturn(Optional.empty());
        when(meterService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meter.getMRID()).thenReturn(null);
        when(meter.getName()).thenReturn(null);
        when(usagePoint.getMRID()).thenReturn(null);
        when(usagePoint.getName()).thenReturn(null);
        METER_MRID = null;
        METER_NAME = null;
        USAGE_POINT_MRID = null;
        USAGE_POINT_NAME = null;
        doTestTransitionToEndStateShouldSendResponse(DefaultState.FAILED);
    }

    @Test
    public void testTransitionToSuccessfulShouldSendResponse() {
        doTestTransitionToEndStateShouldSendResponse(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testTransitionToPartialSuccessShouldSendResponse() {
        doTestTransitionToEndStateShouldSendResponse(DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void testChildTransitionToSuccessfullAndRequestPartialSuccess() {
        handler.onChildStateChange(serviceCall, childServiceCallSuccess, DefaultState.ONGOING, DefaultState.SUCCESSFUL);

        verify(masterExtension)
                .setActualNumberOfSuccessfulCalls(eq(BigDecimal.valueOf(NUMBER_OF_SUCCESSFUL_CALLS.intValue() + 1)));
        verify(serviceCall).update(masterExtension);
        verify(serviceCall).requestTransition(DefaultState.PARTIAL_SUCCESS);

    }

    @Test
    public void testChildTransitionToFailedAndRequestPartialSuccess() {
        handler.onChildStateChange(serviceCall, childServiceCallSuccess, DefaultState.ONGOING, DefaultState.FAILED);

        verify(masterExtension)
                .setActualNumberOfFailedCalls(eq(BigDecimal.valueOf(NUMBER_OF_FAILED_CALLS.intValue() + 1)));
        verify(serviceCall).update(masterExtension);
        verify(serviceCall).requestTransition(DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void testChildTransitionToFailed() {
        when(masterExtension.getActualNumberOfSuccessfulCalls()).thenReturn(BigDecimal.ZERO);
        when(masterExtension.getExpectedNumberOfCalls())
                .thenReturn(BigDecimal.valueOf(NUMBER_OF_FAILED_CALLS.intValue() + 1));

        handler.onChildStateChange(serviceCall, childServiceCallSuccess, DefaultState.ONGOING, DefaultState.FAILED);

        verify(masterExtension)
                .setActualNumberOfFailedCalls(eq(BigDecimal.valueOf(NUMBER_OF_FAILED_CALLS.intValue() + 1)));
        verify(serviceCall).update(masterExtension);
        verify(serviceCall).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void testChildTransitionToSuccess() {
        when(masterExtension.getActualNumberOfFailedCalls()).thenReturn(BigDecimal.ZERO);
        when(masterExtension.getExpectedNumberOfCalls())
                .thenReturn(BigDecimal.valueOf(NUMBER_OF_SUCCESSFUL_CALLS.intValue() + 1));

        handler.onChildStateChange(serviceCall, childServiceCallSuccess, DefaultState.ONGOING, DefaultState.SUCCESSFUL);

        verify(masterExtension)
                .setActualNumberOfSuccessfulCalls(eq(BigDecimal.valueOf(NUMBER_OF_SUCCESSFUL_CALLS.intValue() + 1)));
        verify(serviceCall).update(masterExtension);
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testChildTransitionToSuccessAndStillWaiting() {
        when(masterExtension.getExpectedNumberOfCalls()).thenReturn(
                BigDecimal.valueOf(NUMBER_OF_FAILED_CALLS.intValue() + NUMBER_OF_SUCCESSFUL_CALLS.intValue() + 2));

        handler.onChildStateChange(serviceCall, childServiceCallSuccess, DefaultState.ONGOING, DefaultState.SUCCESSFUL);

        verify(masterExtension)
                .setActualNumberOfSuccessfulCalls(eq(BigDecimal.valueOf(NUMBER_OF_SUCCESSFUL_CALLS.intValue() + 1)));
        verify(serviceCall).update(masterExtension);
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    private void doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoReplyWebServiceImplementation(
            DefaultState endState) {
        replyMasterDataLinkageConfigWebServiceHolder.unsetObject();

        handler.onStateChange(serviceCall, DefaultState.ONGOING, endState);

        verifyNoMoreInteractions(endPointConfiguration);
    }

    private void doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoActiveEndpoint(DefaultState endState) {
        when(endPointConfiguration.isActive()).thenReturn(false);

        handler.onStateChange(serviceCall, DefaultState.ONGOING, endState);

        verifyNoMoreInteractions(replyMasterDataLinkageConfigWebService);
    }

    private void doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoOutboundEndpoint(DefaultState endState) {
        when(endPointConfiguration.isInbound()).thenReturn(true);

        handler.onStateChange(serviceCall, DefaultState.ONGOING, endState);

        verifyNoMoreInteractions(replyMasterDataLinkageConfigWebService);
    }

    private void doTestTransitionToEndStateShouldNotSendResponseWhenThereIsNoEndpointWithProvidedCallbackUrl(
            DefaultState endState) {
        when(endPointConfiguration.getUrl()).thenReturn("not really " + CALLBACK_URL);

        handler.onStateChange(serviceCall, DefaultState.ONGOING, endState);

        verifyNoMoreInteractions(replyMasterDataLinkageConfigWebService);
    }

    private void doTestTransitionToEndStateShouldSendResponse(DefaultState endState) {
        BigDecimal expectedNumberOfCalls = BigDecimal.ONE;
        when(masterExtension.getExpectedNumberOfCalls()).thenReturn(expectedNumberOfCalls);
        doAnswer(new Answer<Void>() {
            @SuppressWarnings("unchecked")
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                List<LinkageOperation> linkageOperations = invocation.getArgumentAt(2, List.class);
                assertThat(linkageOperations).hasSize(1);
                verifyLinkageOperation(linkageOperations);
                List<FailedLinkageOperation> failedLinkageOperations = invocation.getArgumentAt(3, List.class);
                assertThat(failedLinkageOperations).hasSize(1);
                verifyLinkageOperation(failedLinkageOperations);
                FailedLinkageOperation failedLinkageOperation = failedLinkageOperations.get(0);
                assertThat(failedLinkageOperation.getErrorCode()).isEqualTo(ERROR_CODE);
                assertThat(failedLinkageOperation.getErrorMessage()).isEqualTo(ERROR_MESSAGE);

                return null;
            }

            private void verifyLinkageOperation(List<? extends LinkageOperation> linkageOperations) {
                LinkageOperation linkageOperation = linkageOperations.get(0);
                assertThat(linkageOperation.getMeterMrid()).isEqualTo(METER_MRID);
                assertThat(linkageOperation.getMeterName()).isEqualTo(METER_NAME);
                assertThat(linkageOperation.getUsagePointMrid()).isEqualTo(USAGE_POINT_MRID);
                assertThat(linkageOperation.getUsagePointName()).isEqualTo(USAGE_POINT_NAME);
            }
        }).when(replyMasterDataLinkageConfigWebService).call(eq(endPointConfiguration), eq(OPERATION.name()),
                anyListOf(LinkageOperation.class), anyListOf(FailedLinkageOperation.class), eq(expectedNumberOfCalls), eq(CORRELARION_ID));

        handler.onStateChange(serviceCall, DefaultState.ONGOING, endState);
        
        verify(replyMasterDataLinkageConfigWebService).call(eq(endPointConfiguration), eq(OPERATION.name()),
                anyListOf(LinkageOperation.class), anyListOf(FailedLinkageOperation.class), eq(expectedNumberOfCalls), eq(CORRELARION_ID));
    }

}
