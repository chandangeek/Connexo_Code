package com.energyict.mdc.device.data.impl.ami.servicecall.handlers;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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

    @Mock
    Thesaurus thesaurus;
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
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        CommandServiceCallDomainExtension commandServiceCallDomainExtension = new CommandServiceCallDomainExtension();
        commandServiceCallDomainExtension.setNrOfUnconfirmedDeviceCommands(1);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(commandServiceCallDomainExtension));
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));
    }

    @Test
    public void testStateChangeFromPendingToOngoingIgnored() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus);

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall, never()).getExtensionFor(any(CommandCustomPropertySet.class));
        verify(serviceCall, never()).requestTransition(any(DefaultState.class));
    }

    @Test
    public void testStateChangeFromWaitingToOngoingNotAllMessagesConfirmed() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setNrOfUnconfirmedDeviceCommands(10);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.WAITING);
    }

    @Test
    public void testStateChangeFromWaitingToOngoingAllMessagesConfirmed() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus);
        CommandServiceCallDomainExtension domainExtension = new CommandServiceCallDomainExtension();
        domainExtension.setNrOfUnconfirmedDeviceCommands(0);
        when(serviceCall.getExtensionFor(any(CommandCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);

        // Asserts
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
    }

    @Test
    public void testStateChangeToSuccessful() throws Exception {
        AbstractOperationServiceCallHandler serviceCallHandler = new EnableLoadLimitServiceCallHandler(messageService, thesaurus);
        CompletionOptionsServiceCallDomainExtension domainExtension = new CompletionOptionsServiceCallDomainExtension();
        domainExtension.setDestinationSpec(DESTINATION_SPEC);
        domainExtension.setDestinationMessage(DESTIONATION_MSG);
        when(serviceCall.getExtensionFor(any(CompletionOptionsCustomPropertySet.class))).thenReturn(Optional.of(domainExtension));
        when(messageService.getDestinationSpec(DESTINATION_SPEC)).thenReturn(Optional.of(destinationSpec));

        // Business method
        serviceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.SUCCESSFUL);

        // Asserts
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(destinationSpec).message(messageCaptor.capture());
        assertEquals(DESTIONATION_MSG, messageCaptor.getValue());
    }
}