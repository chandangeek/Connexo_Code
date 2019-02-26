/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.LinkageInfo;
import com.elster.jupiter.cim.webservices.inbound.soap.OperationEnum;
import com.elster.jupiter.cim.webservices.inbound.soap.masterdatalinkageconfig.MasterDataLinkageHandler;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigFaultMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import com.google.common.collect.ImmutableSet;

import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.time.Clock;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MasterDataLinkageConfigServiceCallHandlerTest {
    private MasterDataLinkageConfigServiceCallHandler handler;

    @Mock
    private ServiceCall serviceCall;
    @Mock
    private JsonService jsonService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Provider<MasterDataLinkageHandler> masterDataLinkageHandlerProvider;
    @Mock
    private Clock clock;
    @Mock
    private MasterDataLinkageConfigDomainExtension extension;
    @Mock
    private MasterDataLinkageHandler masterDataLinkageHandler;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MasterDataLinkageConfigFaultMessageType faultInfo;
    @Mock
    private ErrorType errorType;

    @Before
    public void setup() throws FaultMessage {
        handler = new MasterDataLinkageConfigServiceCallHandler(masterDataLinkageHandlerProvider, clock, jsonService,
                thesaurus);
        when(serviceCall.getExtension(MasterDataLinkageConfigDomainExtension.class)).thenReturn(Optional.of(extension));
        when(masterDataLinkageHandlerProvider.get()).thenReturn(masterDataLinkageHandler);
        when(masterDataLinkageHandler.forLinkageInfo(any(LinkageInfo.class))).thenReturn(masterDataLinkageHandler);
    }

    @Test
    public void testTransitionToPending() {
        handler.onStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING);

        verify(serviceCall).requestTransition(DefaultState.ONGOING);
    }

    @Test
    public void testLink() throws FaultMessage {
        when(extension.getOperation()).thenReturn(OperationEnum.LINK.getOperation());

        handler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        verify(masterDataLinkageHandler).createLinkage();
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testLinkThrowsFaultMessage() throws FaultMessage {
        when(extension.getOperation()).thenReturn(OperationEnum.LINK.getOperation());
        when(masterDataLinkageHandler.createLinkage()).thenThrow(new FaultMessage("message", faultInfo));
        when(faultInfo.getReply().getError().stream().findFirst()).thenReturn(Optional.of(errorType));
        String myErrorDetails = "my error details";
        when(errorType.getDetails()).thenReturn(myErrorDetails);
        String myErrorCode = "my error code";
        when(errorType.getCode()).thenReturn(myErrorCode);

        handler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        verify(masterDataLinkageHandler).createLinkage();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorCode(OperationEnum.LINK.getDefaultErrorCode());
        verify(extension).setErrorCode(myErrorCode);
        verify(extension).setErrorMessage(myErrorDetails);
    }

    @Test
    public void testLinkThrowsFaultMessageWithoutErrorType() throws FaultMessage {
        when(extension.getOperation()).thenReturn(OperationEnum.LINK.getOperation());
        String myErrorMessage = "my error message";
        when(masterDataLinkageHandler.createLinkage()).thenThrow(new FaultMessage(myErrorMessage, faultInfo));
        when(faultInfo.getReply().getError().stream().findFirst()).thenReturn(Optional.empty());

        handler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        verify(masterDataLinkageHandler).createLinkage();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorCode(OperationEnum.LINK.getDefaultErrorCode());

        verify(extension).setErrorMessage(myErrorMessage);
    }

    @Test
    public void testLinkThrowsConstraintViolationExceptionWithoutConstraintViolations() throws FaultMessage {
        when(extension.getOperation()).thenReturn(OperationEnum.LINK.getOperation());
        String myErrorMessage = "my error message";
        when(masterDataLinkageHandler.createLinkage())
                .thenThrow(new ConstraintViolationException(myErrorMessage, Collections.emptySet()));

        handler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        verify(masterDataLinkageHandler).createLinkage();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorCode(OperationEnum.LINK.getDefaultErrorCode());

        verify(extension).setErrorMessage(myErrorMessage);
    }

    @Test
    public void testLinkThrowsConstraintViolationException() throws FaultMessage {
        when(extension.getOperation()).thenReturn(OperationEnum.LINK.getOperation());
        ConstraintViolation<?> constraintViolation = mock(ConstraintViolation.class);
        String errorMessageFromConstraintViolation = "error message from constraintViolation";
        when(constraintViolation.getMessage()).thenReturn(errorMessageFromConstraintViolation);
        when(masterDataLinkageHandler.createLinkage())
                .thenThrow(new ConstraintViolationException("my error message", ImmutableSet.of(constraintViolation)));

        handler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        verify(masterDataLinkageHandler).createLinkage();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorCode(OperationEnum.LINK.getDefaultErrorCode());

        verify(extension).setErrorMessage(errorMessageFromConstraintViolation);
    }

    @Test
    public void testLinkThrowsSomeDifferentException() throws FaultMessage {
        when(extension.getOperation()).thenReturn(OperationEnum.LINK.getOperation());
        String errorMessage = "some error";
        when(masterDataLinkageHandler.createLinkage()).thenThrow(new RuntimeException(errorMessage));

        handler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        verify(masterDataLinkageHandler).createLinkage();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorCode(OperationEnum.LINK.getDefaultErrorCode());

        verify(extension).setErrorMessage(errorMessage);
    }

    @Test
    public void testUnLink() throws FaultMessage {
        when(extension.getOperation()).thenReturn(OperationEnum.UNLINK.getOperation());

        handler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        verify(masterDataLinkageHandler).closeLinkage();
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }
}
