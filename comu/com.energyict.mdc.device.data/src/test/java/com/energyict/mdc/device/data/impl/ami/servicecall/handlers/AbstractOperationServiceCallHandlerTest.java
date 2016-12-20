package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ami.CompletionOptionsCallBack;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandOperationStatus;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 15/06/2016 - 13:49
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractOperationServiceCallHandlerTest {

    private static final long SERVICE_CALL_ID = 1;
    private static final String DESTINATION_SPEC = "destination spec";
    private static final String DESTIONATION_MSG = "destination msg";
    private static final long DEVICE_MESSAGE_ID_1 = 1L;
    private static final long DEVICE_MESSAGE_ID_2 = 2L;
    private static final long DEVICE_MESSAGE_ID_3 = 3L;
    private static final String CONSTRAINT_VIOLATION_MESSAGE = "constraintViolation message";

    @Mock
    Thesaurus thesaurus;
    @Mock
    CompletionOptionsCallBack completionOptionsCallBack;
    @Mock
    MessageService messageService;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private DestinationSpec destinationSpec;

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
        when(serviceCall.getParent()).thenReturn(Optional.empty());
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        CommandServiceCallDomainExtension commandServiceCallDomainExtension = new CommandServiceCallDomainExtension();
        commandServiceCallDomainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        commandServiceCallDomainExtension.setNrOfUnconfirmedDeviceCommands(1);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(commandServiceCallDomainExtension));
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));
    }

    @Test
    public void testStateChangeFromPendingToOngoingIgnored() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus, completionOptionsCallBack);

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall, never()).getExtensionFor(any(CommandCustomPropertySet.class));
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testStateChangeFromWaitingToOngoingNotAllMessagesConfirmed() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus, completionOptionsCallBack);
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
        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        domainExtension.setNrOfUnconfirmedDeviceCommands(0);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testStateChangeToSuccessful() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus, completionOptionsCallBack);
        CompletionOptionsServiceCallDomainExtension domainExtension = new CompletionOptionsServiceCallDomainExtension();
        domainExtension.setDestinationSpec(DESTINATION_SPEC);
        domainExtension.setDestinationIdentification(DESTIONATION_MSG);
        when(serviceCall.getExtensionFor(any(CompletionOptionsCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));
        when(messageService.getDestinationSpec(DESTINATION_SPEC)).thenReturn(Optional.of(destinationSpec));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.SUCCESSFUL);

        // Asserts
        verify(completionOptionsCallBack).sendFinishedMessageToDestinationSpec(serviceCall);
    }

    @Test
    public void testStateChangeFromWaitingToCancelled() throws Exception {
        Device device = mock(Device.class);
        DeviceMessage deviceMessage1 = mock(DeviceMessage.class);
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessage deviceMessage3 = mock(DeviceMessage.class);
        when(deviceMessage1.getId()).thenReturn(DEVICE_MESSAGE_ID_1);
        when(deviceMessage2.getId()).thenReturn(DEVICE_MESSAGE_ID_2);
        when(deviceMessage3.getId()).thenReturn(DEVICE_MESSAGE_ID_3);
        when(deviceMessage1.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(deviceMessage2.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(deviceMessage3.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(new ArrayList<>(Arrays.asList(deviceMessage1, deviceMessage3)));
        when(device.getMessagesByState(DeviceMessageStatus.WAITING)).thenReturn(new ArrayList<>(Collections.singletonList(deviceMessage2)));
        doReturn(Optional.of(device)).when(serviceCall).getTargetObject();

        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        domainExtension.setNrOfUnconfirmedDeviceCommands(2);
        domainExtension.setDeviceMessages(Arrays.asList(deviceMessage1, deviceMessage2));
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.CANCELLED);

        // Asserts
        verify(deviceMessage1).revoke();
        verify(deviceMessage2).revoke();
        verify(deviceMessage3, never()).revoke(); // deviceMessage3 was pending, but not part of the service call - therefore it should not be revoked!
        verify(completionOptionsCallBack).sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.CANCELLED, CompletionMessageInfo.FailureReason.SERVICE_CALL_HAS_BEEN_CANCELLED);
    }

    @Test
    public void testStateChangeFromWaitingToCancelledRevokeOfDeviceMessageFailed() throws Exception {
        Device device = mock(Device.class);
        DeviceMessage deviceMessage1 = mock(DeviceMessage.class);
        DeviceMessage deviceMessage2 = mock(DeviceMessage.class);
        DeviceMessage deviceMessage3 = mock(DeviceMessage.class);
        when(deviceMessage1.getId()).thenReturn(DEVICE_MESSAGE_ID_1);
        when(deviceMessage2.getId()).thenReturn(DEVICE_MESSAGE_ID_2);
        when(deviceMessage3.getId()).thenReturn(DEVICE_MESSAGE_ID_3);
        when(deviceMessage1.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        when(deviceMessage2.getStatus()).thenReturn(DeviceMessageStatus.WAITING);
        when(deviceMessage3.getStatus()).thenReturn(DeviceMessageStatus.PENDING);
        ConstraintViolation constraintViolation = mock(ConstraintViolation.class);
        ConstraintViolationException constraintViolationException = mock(ConstraintViolationException.class);
        when(constraintViolation.getMessage()).thenReturn(CONSTRAINT_VIOLATION_MESSAGE);
        doReturn(Collections.singleton(constraintViolation)).when(constraintViolationException).getConstraintViolations();
        doThrow(constraintViolationException).when(deviceMessage1).revoke();
        when(device.getMessagesByState(DeviceMessageStatus.PENDING)).thenReturn(new ArrayList<>(Arrays.asList(deviceMessage1, deviceMessage3)));
        when(device.getMessagesByState(DeviceMessageStatus.WAITING)).thenReturn(new ArrayList<>(Collections.singletonList(deviceMessage2)));
        doReturn(Optional.of(device)).when(serviceCall).getTargetObject();

        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus, completionOptionsCallBack);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setCommandOperationStatus(CommandOperationStatus.SEND_OUT_DEVICE_MESSAGES);
        domainExtension.setNrOfUnconfirmedDeviceCommands(2);
        domainExtension.setDeviceMessages(Arrays.asList(deviceMessage1, deviceMessage2));
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.CANCELLED);

        // Asserts
        verify(deviceMessage1).revoke();
        verify(deviceMessage2).revoke();
        verify(deviceMessage3, never()).revoke(); // deviceMessage3 was pending, but not part of the service call - therefore it should not be revoked!
        verify(serviceCall).log(LogLevel.SEVERE, MessageFormat.format("Could not revoke device message with id {0}: {1}", deviceMessage1.getId(), CONSTRAINT_VIOLATION_MESSAGE));
        verify(completionOptionsCallBack).sendFinishedMessageToDestinationSpec(serviceCall, CompletionMessageInfo.CompletionMessageStatus.CANCELLED, CompletionMessageInfo.FailureReason.SERVICE_CALL_HAS_BEEN_CANCELLED);
    }
}