/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.Action;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.UsagePointBuilder;
import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.UsagePointBuilder.PreparedUsagePointBuilder;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.json.JsonService;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigFaultMessageType;
import com.google.common.collect.ImmutableSet;

import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointConfigServiceCallHandlerTest {
    private UsagePointConfigServiceCallHandler handler;
    @Mock
    private Provider<UsagePointBuilder> usagePointBuilderProvider;
    @Mock
    private JsonService jsonService;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private UsagePointConfigDomainExtension extension;
    @Mock
    private UsagePointBuilder builder;
    @Mock
    private PreparedUsagePointBuilder preparedBuilder;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UsagePointConfigFaultMessageType faultInfo;
    @Mock
    private ErrorType errorType;

    @Before
    public void setup() {
        handler = new UsagePointConfigServiceCallHandler(usagePointBuilderProvider, jsonService);
        when(serviceCall.getExtension(UsagePointConfigDomainExtension.class)).thenReturn(Optional.of(extension));
        when(usagePointBuilderProvider.get()).thenReturn(builder);
        when(builder.from(any(UsagePoint.class), anyInt())).thenReturn(preparedBuilder);
    }

    @Test
    public void testCreate() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.CREATE.toString());

        handler.process(serviceCall);

        verify(preparedBuilder).create();
        verifyNoMoreInteractions(preparedBuilder);
    }

    @Test
    public void testCreateWithRequestTimestamp() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.CREATE.name());
        Instant requestTimestamp = Instant.now();
        when(extension.getRequestTimestamp()).thenReturn(requestTimestamp);

        handler.process(serviceCall);

        verify(preparedBuilder).create();
        verify(preparedBuilder).at(requestTimestamp);
    }

    @Test
    public void testOperationThrowsFaultMessage() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.CREATE.name());
        when(preparedBuilder.create()).thenThrow(new FaultMessage("message", faultInfo));
        when(faultInfo.getReply().getError().stream().findFirst()).thenReturn(Optional.of(errorType));
        String myErrorDetails = "my error details";
        when(errorType.getDetails()).thenReturn(myErrorDetails);
        String myErrorCode = "my error code";
        when(errorType.getCode()).thenReturn(myErrorCode);

        handler.process(serviceCall);

        verify(preparedBuilder).create();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorCode(myErrorCode);
        verify(extension).setErrorMessage(myErrorDetails);
    }

    @Test
    public void testOperationThrowsFaultMessageWithoutErrorType() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.CREATE.name());
        String myErrorMessage = "my error message";
        when(preparedBuilder.create()).thenThrow(new FaultMessage(myErrorMessage, faultInfo));
        when(faultInfo.getReply().getError().stream().findFirst()).thenReturn(Optional.empty());

        handler.process(serviceCall);

        verify(preparedBuilder).create();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorMessage(myErrorMessage);
    }

    @Test
    public void testOperationThrowsConstraintViolationExceptionWithoutConstraintViolations() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.CREATE.name());
        String myErrorMessage = "my error message";
        when(preparedBuilder.create())
                .thenThrow(new ConstraintViolationException(myErrorMessage, Collections.emptySet()));

        handler.process(serviceCall);

        verify(preparedBuilder).create();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorMessage(myErrorMessage);
    }

    @Test
    public void testOperationThrowsConstraintViolationException() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.CREATE.name());
        ConstraintViolation<?> constraintViolation = mock(ConstraintViolation.class);
        String errorMessageFromConstraintViolation = "error message from constraintViolation";
        when(constraintViolation.getMessage()).thenReturn(errorMessageFromConstraintViolation);
        when(preparedBuilder.create())
                .thenThrow(new ConstraintViolationException("my error message", ImmutableSet.of(constraintViolation)));

        handler.process(serviceCall);

        verify(preparedBuilder).create();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorMessage(errorMessageFromConstraintViolation);
    }

    @Test
    public void testOperationThrowsSomeDifferentException() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.CREATE.name());
        String errorMessage = "some error";
        when(preparedBuilder.create()).thenThrow(new RuntimeException(errorMessage));

        handler.process(serviceCall);

        verify(preparedBuilder).create();
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(extension).setErrorMessage(errorMessage);
    }

    @Test
    public void testUpdate() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.UPDATE.toString());

        handler.process(serviceCall);

        verify(preparedBuilder).update();
        verifyNoMoreInteractions(preparedBuilder);
    }

    @Test
    public void testUpdateWithRequestTimestamp() throws FaultMessage {
        when(extension.getOperation()).thenReturn(Action.UPDATE.toString());
        Instant requestTimestamp = Instant.now();
        when(extension.getRequestTimestamp()).thenReturn(requestTimestamp);

        handler.process(serviceCall);

        verify(preparedBuilder).update();
        verifyNoMoreInteractions(preparedBuilder);
    }

}
