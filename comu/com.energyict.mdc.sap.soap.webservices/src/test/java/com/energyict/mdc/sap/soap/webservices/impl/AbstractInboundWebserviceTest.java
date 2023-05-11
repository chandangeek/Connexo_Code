/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.AbstractEndPointInitializer;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import javax.validation.MessageInterpolator;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractInboundWebserviceTest {
    @Mock
    protected ThreadPrincipalService threadPrincipalService;
    @Mock
    protected UserService userService;
    @Mock
    protected WebServicesService webServicesService;
    @Mock
    protected WebServiceCallOccurrenceService webServiceCallOccurrenceService;
    @Mock
    protected EndPointConfigurationService endPointConfigurationService;
    @Mock
    protected EventService eventService;
    @Mock
    protected WebServiceContext webServiceContext;
    @Mock
    protected MessageContext messageContext;
    @Mock
    protected InboundEndPointConfiguration endPointConfiguration;
    @Mock
    protected WebServiceCallOccurrence webServiceCallOccurrence;

    protected Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    protected TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(WebServicesService.class).toInstance(webServicesService);
                bind(WebServiceCallOccurrenceService.class).toInstance(webServiceCallOccurrenceService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(EventService.class).toInstance(eventService);
                bind(WebServiceContext.class).toInstance(webServiceContext);
                bind(InboundEndPointConfiguration.class).toInstance(endPointConfiguration);
            }
        };
    }

    protected <T extends InboundSoapEndPointProvider> T getProviderInstance(Class<T> providerClass, Module... modules) {
        Injector injector = Guice.createInjector(Stream.concat(Stream.of(getModule()), Arrays.stream(modules)).toArray(Module[]::new));
        T provider = injector.getInstance(providerClass);
        AbstractEndPointInitializer initializer = injector.getInstance(AbstractEndPointInitializer.class);
        Object webService = provider.get();
        initializer.initializeInboundEndPoint(webService, endPointConfiguration);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, webService, "webServiceContext", webServiceContext);

        long occurrenceId = webServiceCallOccurrence.getId();
        when(messageContext.get(WebServiceCallOccurrence.MESSAGE_CONTEXT_OCCURRENCE_ID)).thenReturn(occurrenceId);
        when(webServiceCallOccurrenceService.getOngoingOccurrence(occurrenceId)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.empty());
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.empty());
        return provider;
    }

    private static void inject(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertThrowsException(ThrowableAssert.ThrowingCallable callable, Class<? extends Exception> exceptionClass, String message) {
        assertThatThrownBy(callable)
                .isInstanceOf(exceptionClass)
                .hasMessage(message);
        ArgumentCaptor<? extends Exception> captor = ArgumentCaptor.forClass(exceptionClass);
        verify(webServiceCallOccurrenceService).failOccurrence(eq(webServiceCallOccurrence.getId()), captor.capture());
        assertThat(captor.getValue().getLocalizedMessage()).isEqualTo(message);
    }
}
