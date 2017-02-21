/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.StatusInformationTask;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 15/06/2016 - 14:15
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractContactorOperationServiceCallHandlerTest {

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    private static final long SERVICE_CALL_ID = 1;
    private static final String DESTINATION_SPEC = "destination spec";
    private static final String DESTIONATION_MSG = "destination msg";

    @Mock
    Thesaurus thesaurus;
    @Mock
    CompletionOptionsCallBack completionOptionsCallBack;
    @Mock
    MessageService messageService;
    @Mock
    DeviceService deviceService;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    Device device;
    @Mock
    DeviceConfiguration deviceConfiguration;
    @Mock
    private ComTaskExecution comTaskExecution;

    @Before
    public void setUp() throws Exception {
        when(thesaurus.getFormat(any(TranslationKey.class))).thenAnswer(invocationOnMock -> {
            TranslationKey translationKey = (TranslationKey) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(translationKey.getDefaultFormat(), args);
                }
            };
        });
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocationOnMock -> {
            MessageSeed messageSeed = (MessageSeed) invocationOnMock.getArguments()[0];
            return new NlsMessageFormat() {
                @Override
                public String format(Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }

                @Override
                public String format(Locale locale, Object... args) {
                    return MessageFormat.format(messageSeed.getDefaultFormat(), args);
                }
            };
        });

        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        CommandServiceCallDomainExtension commandServiceCallDomainExtension = new CommandServiceCallDomainExtension();
        commandServiceCallDomainExtension.setNrOfUnconfirmedDeviceCommands(1);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(commandServiceCallDomainExtension));
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));
        doReturn(Optional.of(device)).when(serviceCall).getTargetObject();

        deviceConfiguration = mock(DeviceConfiguration.class);
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        ComTask comTask = mock(ComTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Collections.singletonList(new StatusInformationTask() {
            @Override
            public ComTask getComTask() {
                return null;
            }

            @Override
            public long getId() {
                return 0;
            }

            @Override
            public void save() {

            }
        }));
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
    }

    @Test
    public void testStateChangeFromPendingToOngoingIgnored() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack);

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testStateChangeFromWaitingToOngoingNotAllMessagesConfirmed() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        domainExtension.setNrOfUnconfirmedDeviceCommands(10);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.WAITING);
    }

    @Test
    public void testStateChangeFromWaitingToOngoingAllMessagesConfirmed() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        domainExtension.setNrOfUnconfirmedDeviceCommands(0);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        ArgumentCaptor<CommandServiceCallDomainExtension> domainExtensionArgumentCaptor = ArgumentCaptor.forClass(CommandServiceCallDomainExtension.class);
        verify(serviceCall).update(domainExtensionArgumentCaptor.capture());
        assertEquals(CommandOperationStatus.READ_STATUS_INFORMATION, domainExtensionArgumentCaptor.getValue().getCommandOperationStatus());

        verify(comTaskExecution).scheduleNow();
        verify(serviceCall).requestTransition(DefaultState.WAITING);
    }

    @Test
    @Expected(value = IllegalStateException.class, message = "A comtask to read out the status information could not be located")
    public void testStateChangeFromWaitingToOngoingStatusInformationComTaskEnablementNotFound() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        domainExtension.setNrOfUnconfirmedDeviceCommands(0);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));
        ActivatedBreakerStatus breakerStatus = mock(ActivatedBreakerStatus.class);
        when(breakerStatus.getBreakerStatus()).thenReturn(BreakerStatus.DISCONNECTED);
        when(deviceService.getActiveBreakerStatus(device)).thenReturn(Optional.of(breakerStatus));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.emptyList());

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(completionOptionsCallBack).sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.INCORRECT_DEVICE_BREAKER_STATUS);
    }

    @Test
    public void testStateChangeFromWaitingToOngoingStatusInformationReadBreakerStatusNotFound() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        domainExtension.setNrOfUnconfirmedDeviceCommands(0);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));
        when(deviceService.getActiveBreakerStatus(device)).thenReturn(Optional.empty());

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.WAITING);
        verify(serviceCall, never()).requestTransition(DefaultState.SUCCESSFUL);
        verify(serviceCall, never()).requestTransition(DefaultState.FAILED);
    }

    @Test
    public void testStateChangeFromWaitingToOngoingStatusInformationReadBreakerStatusMatches() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        domainExtension.setNrOfUnconfirmedDeviceCommands(0);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        ActivatedBreakerStatus breakerStatus = mock(ActivatedBreakerStatus.class);
        when(breakerStatus.getBreakerStatus()).thenReturn(BreakerStatus.DISCONNECTED);
        when(deviceService.getActiveBreakerStatus(device)).thenReturn(Optional.of(breakerStatus));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testStateChangeFromWaitingToOngoingStatusInformationReadBreakerStatusDoesNotMatches() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.READ_STATUS_INFORMATION);
        domainExtension.setNrOfUnconfirmedDeviceCommands(0);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        ActivatedBreakerStatus breakerStatus = mock(ActivatedBreakerStatus.class);
        when(breakerStatus.getBreakerStatus()).thenReturn(BreakerStatus.CONNECTED);
        when(deviceService.getActiveBreakerStatus(device)).thenReturn(Optional.of(breakerStatus));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.FAILED);
        verify(completionOptionsCallBack).sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.FAILURE, CompletionMessageInfo.FailureReason.INCORRECT_DEVICE_BREAKER_STATUS);
    }
}