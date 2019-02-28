/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.FailedLinkageOperation;
import com.elster.jupiter.cim.webservices.inbound.soap.LinkageOperation;
import com.elster.jupiter.cim.webservices.inbound.soap.ReplyMasterDataLinkageConfigWebService;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ObjectHolder;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageAction;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.MeterInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.UsagePointInfo;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.json.JsonService;

import java.math.BigDecimal;
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

    @Before
    public void setup() {
        replyMasterDataLinkageConfigWebServiceHolder = new ObjectHolder<>();
        replyMasterDataLinkageConfigWebServiceHolder.setObject(replyMasterDataLinkageConfigWebService);
        handler = new MasterDataLinkageConfigMasterServiceCallHandler(endPointConfigurationService,
                replyMasterDataLinkageConfigWebServiceHolder, jsonService);
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
        when(serviceCall.canTransitionTo(DefaultState.SUCCESSFUL)).thenReturn(true);
        when(serviceCall.canTransitionTo(DefaultState.FAILED)).thenReturn(true);
        when(serviceCall.canTransitionTo(DefaultState.PARTIAL_SUCCESS)).thenReturn(true);
        when(jsonService.deserialize(anyString(), eq(UsagePointInfo.class))).thenReturn(usagePointInfo);
        when(jsonService.deserialize(anyString(), eq(MeterInfo.class))).thenReturn(meterInfo);
        when(serviceCall.findChildren()).thenReturn(finder);
        doAnswer(// we cannot use thenReturn because the call happens more than once and stream is closed after first call
                new Answer<Stream<ServiceCall>>() {
                    @Override
                    public Stream<ServiceCall> answer(InvocationOnMock invocation) throws Throwable {
                        return Stream.of(childServiceCallSuccess, childServiceCallFailure);
                    }
                }).when(finder).stream();
        when(childServiceCallSuccess.getExtension(MasterDataLinkageConfigDomainExtension.class))
                .thenReturn(Optional.of(masterDataLinkageConfigDomainExtension));
        when(childServiceCallSuccess.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(childServiceCallFailure.getExtension(MasterDataLinkageConfigDomainExtension.class))
                .thenReturn(Optional.of(masterDataLinkageConfigDomainExtension));
        when(childServiceCallFailure.getState()).thenReturn(DefaultState.FAILED);
        when(masterDataLinkageConfigDomainExtension.getOperation()).thenReturn(MasterDataLinkageAction.CREATE.name());
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
    public void testTransitionToSuccessfulShouldSendResponse() {
        doTestTransitionToEndStateShouldSendResponse(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testTransitionToPartialSuccessShouldSendResponse() {
        doTestTransitionToEndStateShouldSendResponse(DefaultState.PARTIAL_SUCCESS);
    }

    @Test
    public void testTransitionToFailed() {
        handler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.FAILED);

        // TODO
    }

    @Test
    public void testTransitionToSuccessful() {
        handler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.SUCCESSFUL);
        // TODO
    }

    @Test
    public void testTransitionToPartialSuccess() {
        handler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.PARTIAL_SUCCESS);

        // TODO
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

        handler.onStateChange(serviceCall, DefaultState.ONGOING, endState);

        verify(replyMasterDataLinkageConfigWebService).call(eq(endPointConfiguration), eq(OPERATION),
                anyListOf(LinkageOperation.class), anyListOf(FailedLinkageOperation.class), eq(expectedNumberOfCalls));
    }

}
