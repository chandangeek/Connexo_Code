/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.metering.ami.CompletionMessageInfo.CompletionMessageStatus;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationCustomPropertySet;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationDomainExtension;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.metering.ami.CompletionMessageInfo.FailureReason;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 15/07/2016 - 12:11
 */
@RunWith(MockitoJUnitRunner.class)
public class CompletionOptionsHandlerTest {

    private static final long SERVICE_CALL_ID = 1L;
    private static final String CALL_BACK = "http://wwww.myCallbackUri.com";
    private static final boolean PROVIDED_RESPONSE = false;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    UsagePoint usagePoint;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCall childServiceCall_1;
    @Mock
    private ServiceCall childServiceCall_2;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private JsonService jsonService;
    @Mock
    private MessageService messageService;
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private ContactorOperationDomainExtension contactorOperationDomainExtension;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    Message message;
    @Mock
    Client client;
    @Mock
    WebTarget webTarget;
    @Mock
    Builder webTargetBuilder;

    private CompletionMessageInfo completionMessageInfo;
    private CompletionOptionsHandler completionOptionsHandler;

    @Before
    public void setUp() throws Exception {
        setUpThesaurus();
        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        when(childServiceCall_1.canTransitionTo(any(DefaultState.class))).thenReturn(true);
        when(childServiceCall_2.canTransitionTo(any(DefaultState.class))).thenReturn(true);

        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(childServiceCall_1.getType()).thenReturn(serviceCallType);
        when(childServiceCall_2.getType()).thenReturn(serviceCallType);
        when(serviceCallType.getName()).thenReturn("ServiceCallType");

        Finder<ServiceCall> childServiceCallFinder = mock(Finder.class);
        List<ServiceCall> children = new ArrayList<>(2);
        children.add(childServiceCall_1);
        children.add(childServiceCall_2);
        when(childServiceCallFinder.stream()).thenReturn(children.stream());
        when(serviceCall.findChildren()).thenReturn(childServiceCallFinder);
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.of(serviceCall));

        when(contactorOperationDomainExtension.providedResponse()).thenReturn(PROVIDED_RESPONSE);
        when(contactorOperationDomainExtension.getCallback()).thenReturn(CALL_BACK);
        when(contactorOperationDomainExtension.getBreakerStatus()).thenReturn(BreakerStatus.connected);
        when(serviceCall.getExtensionFor(any(ContactorOperationCustomPropertySet.class))).thenReturn(Optional.of(contactorOperationDomainExtension));

        completionMessageInfo = new CompletionMessageInfo(Long.toString(SERVICE_CALL_ID));
        completionMessageInfo.setCompletionMessageStatus(CompletionMessageStatus.FAILURE);
        completionMessageInfo.setFailureReason(FailureReason.INCORRECT_DEVICE_BREAKER_STATUS);
        when(jsonService.serialize(any())).then(i -> i.getArgumentAt(0, CompletionMessageInfo.class).toString());
        when(jsonService.deserialize(any(byte[].class), any(Class.class))).thenReturn(completionMessageInfo);

        CompletionOptionsHandler actualHandler = new CompletionOptionsHandler(jsonService, serviceCallService, thesaurus);
        completionOptionsHandler = Mockito.spy(actualHandler);

        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(webTargetBuilder);
        when(completionOptionsHandler.newJerseyClient()).thenReturn(client);
    }

    private void setUpThesaurus() {
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
    }

    @Test
    @Expected(value = IllegalStateException.class, message = "Could not find service call with ID 1")
    public void testProcessServiceCallNotFound() throws Exception {
        when(serviceCallService.getServiceCall(SERVICE_CALL_ID)).thenReturn(Optional.empty());

        // Business method
        completionOptionsHandler.process(message);
    }

    @Test
    public void testProcessServiceCallNotSuccessful() throws Exception {
        completionMessageInfo = new CompletionMessageInfo(Long.toString(SERVICE_CALL_ID));
        completionMessageInfo.setCompletionMessageStatus(CompletionMessageStatus.SUCCESS);
        when(jsonService.deserialize(any(byte[].class), any(Class.class))).thenReturn(completionMessageInfo);

        // Business method
        completionOptionsHandler.process(message);

        // Asserts
        verify(completionOptionsHandler, never()).newJerseyClient();
    }

    @Test
    public void testProcessAlreadySendOutResponse() throws Exception {
        when(contactorOperationDomainExtension.providedResponse()).thenReturn(true);
        when(contactorOperationDomainExtension.getCallback()).thenReturn(CALL_BACK);
        when(serviceCall.getExtensionFor(any(ContactorOperationCustomPropertySet.class))).thenReturn(Optional.of(contactorOperationDomainExtension));

        // Business method
        completionOptionsHandler.process(message);

        // Asserts
        verify(completionOptionsHandler, never()).newJerseyClient();
    }

    @Test
    @Expected(value = IllegalArgumentException.class, message = "Not possible to send back the response, as the callback uri was not specified")
    public void testInvalidCallBackURI() throws Exception {
        when(contactorOperationDomainExtension.providedResponse()).thenReturn(PROVIDED_RESPONSE);
        when(contactorOperationDomainExtension.getCallback()).thenReturn(null);
        when(serviceCall.getExtensionFor(any(ContactorOperationCustomPropertySet.class))).thenReturn(Optional.of(contactorOperationDomainExtension));

        // Business method
        completionOptionsHandler.process(message);
    }

    @Test
    public void testProvideResponse() throws Exception {
        // Business method
        completionOptionsHandler.process(message);

        // Asserts
        ArgumentCaptor<Entity> argumentCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(webTargetBuilder).post(argumentCaptor.capture());

        assertTrue(argumentCaptor.getValue().getEntity() instanceof ResponseInfo);
        ResponseInfo responseInfo = (ResponseInfo) argumentCaptor.getValue().getEntity();
        assertEquals(CompletionMessageStatus.FAILURE, responseInfo.status);
        assertEquals(FailureReason.INCORRECT_DEVICE_BREAKER_STATUS, responseInfo.reason);

        ArgumentCaptor<ContactorOperationDomainExtension> domainExtensionArgumentCaptor = ArgumentCaptor.forClass(ContactorOperationDomainExtension.class);
        verify(contactorOperationDomainExtension).setProvidedResponse(true);
        verify(serviceCall).update(domainExtensionArgumentCaptor.capture());
        assertEquals(contactorOperationDomainExtension, domainExtensionArgumentCaptor.getValue());
    }

    @Test
    public void testProvideResponseAndUpdateUsagePointConnectionState() throws Exception {
        when(serviceCall.getState()).thenReturn(DefaultState.SUCCESSFUL);
        doReturn(Optional.of(usagePoint)).when(serviceCall).getTargetObject();

        completionMessageInfo = new CompletionMessageInfo(Long.toString(SERVICE_CALL_ID));
        completionMessageInfo.setCompletionMessageStatus(CompletionMessageStatus.SUCCESS);
        jsonService = mock(JsonService.class);
        when(jsonService.serialize(any())).then(i -> i.getArgumentAt(0, CompletionMessageInfo.class).toString());
        when(jsonService.deserialize(any(byte[].class), any(Class.class))).thenReturn(completionMessageInfo);

        CompletionOptionsHandler actualHandler = new CompletionOptionsHandler(jsonService, serviceCallService, thesaurus);
        completionOptionsHandler = Mockito.spy(actualHandler);

        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(webTargetBuilder);
        when(completionOptionsHandler.newJerseyClient()).thenReturn(client);

        // Business method
        completionOptionsHandler.process(message);

        // Asserts
        ArgumentCaptor<Entity> argumentCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(webTargetBuilder).post(argumentCaptor.capture());

        assertTrue(argumentCaptor.getValue().getEntity() instanceof ResponseInfo);
        ResponseInfo responseInfo = (ResponseInfo) argumentCaptor.getValue().getEntity();
        assertEquals(CompletionMessageStatus.SUCCESS, responseInfo.status);
        assertEquals(null, responseInfo.reason);

        ArgumentCaptor<ContactorOperationDomainExtension> domainExtensionArgumentCaptor = ArgumentCaptor.forClass(ContactorOperationDomainExtension.class);
        verify(contactorOperationDomainExtension).setProvidedResponse(true);
        verify(serviceCall).update(domainExtensionArgumentCaptor.capture());
        assertEquals(contactorOperationDomainExtension, domainExtensionArgumentCaptor.getValue());

        verify(usagePoint).setConnectionState(ConnectionState.CONNECTED);
    }
}