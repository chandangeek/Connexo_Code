/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.ModemBasedInboundComPort;
import com.energyict.mdc.common.comserver.OfflineComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.RemoteComServer;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.impl.OfflineComServerImpl;
import com.energyict.mdc.engine.config.impl.OnlineComServerImpl;
import com.energyict.mdc.engine.config.impl.RemoteComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ServerEventAPIStatistics;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.monitor.ComServerMonitor;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import com.google.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    private ManagementBeanFactory managementBeanFactory;
    @Mock(extraInterfaces = ComServerMonitor.class)
    private ComServerMonitorImplMBean comServerMonitor;
    @Mock
    private ServerEventAPIStatistics eventApiStatistics;
    @Mock
    private QueryAPIStatistics queryAPIStatistics;
    @Mock
    private RunningComServerImpl.ServiceProvider serviceProvider;
    @Mock
    Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider;
    @Mock
    Provider<CoapBasedInboundComPort> coapBasedInboundComPortProvider;
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
        when(this.serviceProvider.managementBeanFactory()).thenReturn(this.managementBeanFactory);
        when(this.managementBeanFactory.findOrCreateFor(any(RunningComServer.class))).thenReturn(this.comServerMonitor);
        ComServerMonitor comServerMonitor = (ComServerMonitor) this.comServerMonitor;
        when(comServerMonitor.getEventApiStatistics()).thenReturn(this.eventApiStatistics);
        when(comServerMonitor.getQueryApiStatistics()).thenReturn(this.queryAPIStatistics);
    }

    public void cleanUp(){
        if(embeddedWebServer != null){
            embeddedWebServer.shutdownImmediate();
        }
    }

    private OfflineComServer createOfflineComServer() {
        return new OfflineComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, coapBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Test
    public void testEventsWithOfflineComServer () {
        OfflineComServer comServer = createOfflineComServer();

        // Business method
        embeddedWebServer = this.factory.findOrCreateEventWebServer(comServer, eventApiStatistics);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
    }


    private OnlineComServer createOnlineComServer() {
        return createOnlineComServer(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
    }

    private OnlineComServer createOnlineComServer(int eventRegistrationPort) {
        final OnlineComServerImpl onlineComServer = new OnlineComServerImpl(dataModel, engineConfigurationService, outboundComPortProvider, servletBasedInboundComPortProvider, coapBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
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
        embeddedWebServer = this.factory.findOrCreateEventWebServer(comServer, eventApiStatistics);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
    }

    private RemoteComServer createRemoteComServerWithRegistrationPort() {
        return createRemoteComServerWithRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
    }

    private RemoteComServer createRemoteComServerWithRegistrationPort(int eventRegistrationPort) {
        final RemoteComServerImpl remoteComServer = new RemoteComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, coapBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        remoteComServer.setServerName("RemoteServerName");
        remoteComServer.setStatusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.setEventRegistrationPort(eventRegistrationPort);
        return remoteComServer;
    }

    @Test
    public void testEventsWithRemoteComServer () {
        RemoteComServer comServer = createRemoteComServerWithRegistrationPort();

        // Business method
        embeddedWebServer = this.factory.findOrCreateEventWebServer(comServer, eventApiStatistics);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
    }

    @Test
    public void testQueriesWithOnlineComServer () {
        OnlineComServer comServer = createOnlineComServer();
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);

        // Business method
        embeddedWebServer = this.factory.findOrCreateRemoteQueryWebServer(runningOnlineComServer, queryAPIStatistics);

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
        embeddedWebServer = this.factory.findOrCreateRemoteQueryWebServer(runningOnlineComServer, queryAPIStatistics);

        // Asserts
        assertThat(embeddedWebServer).isNotNull();
        assertThat(embeddedWebServer.getClass().getSimpleName()).startsWith("Void");
    }
}