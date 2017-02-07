/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.engine.config.impl.OfflineComServerImpl;
import com.energyict.mdc.engine.config.impl.OnlineComServerImpl;
import com.energyict.mdc.engine.config.impl.RemoteComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;

import com.google.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DefaultEmbeddedWebServerFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (16:57)
 */
@RunWith(MockitoJUnitRunner.class)
public class EmbeddedWebServerFactoryTest {

    private static final String EVENT_REGISTRATION_URL = "ws://comserver.energyict.com/events/registration";
    private static final String INVALID_URI = "Anything but a valid URL";

    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPort> outboundComPortProvider;
    @Mock
    Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider;
    @Mock
    Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider;
    @Mock
    Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider;
    @Mock
    Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider;
    @Mock
    Thesaurus thesaurus;
    @Mock
    private WebSocketEventPublisherFactory webSocketEventPublisherFactory;

    private EmbeddedWebServerFactory factory;
    private EmbeddedWebServer embeddedWebServer;

    @Before
    public void setupFactoryUnderTest () {
        this.factory = new DefaultEmbeddedWebServerFactory(this.webSocketEventPublisherFactory);
    }

    public void cleanUp(){
        if(embeddedWebServer != null){
            embeddedWebServer.shutdownImmediate();
        }
    }

    private OfflineComServer createOfflineComServer() {
        return new OfflineComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Test
    public void testEventsWithOfflineComServer () {
        OfflineComServer comServer = createOfflineComServer();

        // Business method
        embeddedWebServer = this.factory.findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
        assertThat(embeddedWebServer.getClass().getSimpleName()).startsWith("Void");
    }


    private OnlineComServer createOnlineComServer() {
        return createOnlineComServer(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
    }

    private OnlineComServer createOnlineComServer(int eventRegistrationPort) {
        final OnlineComServerImpl onlineComServer = new OnlineComServerImpl(dataModel, engineConfigurationService, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        onlineComServer.setName("onlineComServerServerName");
        onlineComServer.setServerName("onlineComServerServerName");
        onlineComServer.setEventRegistrationPort(eventRegistrationPort);
        onlineComServer.setStatusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        onlineComServer.setQueryApiPort(ComServer.DEFAULT_QUERY_API_PORT_NUMBER);
        return onlineComServer;
    }

    @Test
    public void testEventsWithOnlineComServer() {
        OnlineComServer comServer = createOnlineComServer();

        // Business method
        embeddedWebServer = this.factory.findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
    }

    private RemoteComServer createRemoteComServerWithRegistrationPort() {
        return createRemoteComServerWithRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
    }

    private RemoteComServer createRemoteComServerWithRegistrationPort(int eventRegistrationPort) {
        final RemoteComServerImpl remoteComServer = new RemoteComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        remoteComServer.setServerName("RemoteServerName");
        remoteComServer.setStatusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.setEventRegistrationPort(eventRegistrationPort);
        return remoteComServer;
    }

    @Test
    public void testEventsWithRemoteComServer () {
        RemoteComServer comServer = createRemoteComServerWithRegistrationPort();

        // Business method
        embeddedWebServer = this.factory.findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
    }

    @Test
    public void testQueriesWithOnlineComServer () {
        OnlineComServer comServer = createOnlineComServer();
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);

        // Business method
        embeddedWebServer = this.factory.findOrCreateRemoteQueryWebServer(runningOnlineComServer);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
        assertThat(embeddedWebServer.getClass().getSimpleName()).doesNotMatch("Void.*");
    }

    @Test
    public void testQueriesWithOnlineComServerThatDoesNotSupportRemoteQueries () {
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(UnsupportedOperationException.class).when(comServer).getQueryApiPostUriIfSupported();
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);

        // Business method
        embeddedWebServer = this.factory.findOrCreateRemoteQueryWebServer(runningOnlineComServer);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
        assertThat(embeddedWebServer.getClass().getSimpleName()).startsWith("Void");
    }
}