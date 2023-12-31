/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
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
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.upl.meterdata.BreakerStatus;

import javax.xml.bind.annotation.XmlElement;
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

    @Mock
    Thesaurus thesaurus;
    @Mock
    CompletionOptionsCallBack completionOptionsCallBack;
    @Mock
    MessageService messageService;
    @Mock
    DeviceMessageService deviceMessageService;
    @Mock
    DeviceService deviceService;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    Device device;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private CommunicationTaskService communicationTaskService;

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

            @XmlElement(name = "type")
            public String getXmlType() {
                return this.getClass().getName();
            }

            public void setXmlType(String ignore) {
                // For xml unmarshalling purposes only
            }
        }));
        when(comTask.isManualSystemTask()).thenReturn(true);
        when(comTaskEnablement.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getComTask()).thenReturn(comTask);
        when(comTaskExecution.getId()).thenReturn(1L);
        when(device.getComTaskExecutions()).thenReturn(Collections.singletonList(comTaskExecution));
        when(deviceConfiguration.getComTaskEnablements()).thenReturn(Collections.singletonList(comTaskEnablement));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(communicationTaskService.findAndLockComTaskExecutionById(1L)).thenReturn(Optional.of(comTaskExecution));
    }

    @Test
    public void testStateChangeFromPendingToOngoingIgnored() {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack, deviceMessageService);

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testStateChangeFromWaitingToOngoingNotAllMessagesConfirmed() {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack, deviceMessageService);
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
    public void testStateChangeFromWaitingToOngoingAllMessagesConfirmed() {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack, deviceMessageService);
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
        verify(serviceCall).requestTransition(DefaultState.WAITING);
    }

    @Test
    public void testStateChangeFromWaitingToOngoingStatusInformationReadBreakerStatusNotFound() {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack, deviceMessageService);
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
    public void testStateChangeFromWaitingToOngoingStatusInformationReadBreakerStatusMatches() {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack, deviceMessageService);
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
    public void testStateChangeFromWaitingToOngoingStatusInformationReadBreakerStatusDoesNotMatches() {
        AbstractOperationServiceCallHandler serviceCallHandler = new DisconnectServiceCallHandler(messageService, deviceService, thesaurus, completionOptionsCallBack, deviceMessageService);
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
