/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
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
import javax.xml.ws.BindingProvider;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractOutboundWebserviceTest<S> {
    @Mock
    protected ThreadPrincipalService threadPrincipalService;
    @Mock
    protected UserService userService;
    @Mock
    protected WebServicesService webServicesService;
    @Mock
    protected EndPointConfigurationService endPointConfigurationService;
    @Mock
    protected EventService eventService;
    @Mock
    protected OutboundEndPointConfiguration outboundEndPointConfiguration;
    protected S endpoint;
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
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(EventService.class).toInstance(eventService);
            }
        };
    }

    protected <T extends AbstractOutboundEndPointProvider<S> & OutboundSoapEndPointProvider> T getProviderInstance(Class<T> providerClass, Module... modules) {
        Injector injector = Guice.createInjector(Stream.concat(Stream.of(getModule()), Arrays.stream(modules)).toArray(Module[]::new));
        T provider = injector.getInstance(providerClass);
        AbstractEndPointInitializer initializer = injector.getInstance(AbstractEndPointInitializer.class);
        initializer.initializeOutboundEndPointProvider(provider);
        Map<String, Object> properties = new HashMap<>(2, 1);
        properties.put(AbstractOutboundEndPointProvider.ENDPOINT_CONFIGURATION_ID_PROPERTY, outboundEndPointConfiguration.getId());
        properties.put(AbstractOutboundEndPointProvider.URL_PROPERTY, outboundEndPointConfiguration.getUrl());
        endpoint = (S) mock(provider.getService(), withSettings().extraInterfaces(BindingProvider.class));
        injectObjectWithProperties(AbstractOutboundEndPointProvider.class, provider, "doAddEndpoint", endpoint, properties);

        String name = getName(provider);
        when(outboundEndPointConfiguration.isActive()).thenReturn(true);
        when(outboundEndPointConfiguration.getWebServiceName()).thenReturn(name);
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(name)).thenReturn(Collections.singletonList(outboundEndPointConfiguration));
        when(webServicesService.startOccurrence(eq(outboundEndPointConfiguration), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        when(webServicesService.startOccurrence(eq(outboundEndPointConfiguration), anyString(), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        long occurrenceId = webServiceCallOccurrence.getId();
        when(webServicesService.getOngoingOccurrence(occurrenceId)).thenReturn(webServiceCallOccurrence);
        return provider;
    }

    private String getName(AbstractOutboundEndPointProvider<?> provider) {
        return spy(AbstractOutboundEndPointProvider.class, provider, "getName", String.class);
    }

    private static void injectObjectWithProperties(Class<?> clazz, Object instance, String methodName, Object argument, Map<?, ?> propertyMap) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, Object.class, Map.class);
            method.setAccessible(true);
            method.invoke(instance, argument, propertyMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T spy(Class<?> clazz, Object instance, String methodName, Class<T> resultClass) {
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return resultClass.cast(method.invoke(instance));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
