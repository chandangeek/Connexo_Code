/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web;

import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import com.energyict.mdc.upl.TypedProperties;
import org.eclipse.jetty.server.Server;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link EmbeddedJettyServer} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-19 (14:59)
 */
@RunWith(MockitoJUnitRunner.class)
public class EmbeddedJettyServerTest {

    private static final int PORT_NUMBER = 9000;

    @Mock
    private WebSocketEventPublisherFactory webSocketEventPublisherFactory;
    @Mock
    private InboundCommunicationHandler.ServiceProvider inboundCommunicationHandlerServiceProvider;
    @Mock
    private EmbeddedJettyServer.ServiceProvider embeddedJettyServerServiceProvider;
    @Mock
    EventAPIStatistics eventAPIStatistics;
    @Mock
    QueryAPIStatistics queryAPIStatistics;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private RunningComServerImpl.ServiceProvider serviceProvider;

    private EmbeddedJettyServer embeddedJettyServer;
    private Server jettyServer;

    @Before
    public void setupServiceProvider () {
        when(this.embeddedJettyServerServiceProvider.webSocketEventPublisherFactory()).thenReturn(this.webSocketEventPublisherFactory);
    }

    @After
    public void stopRunningJettyServer () {
        if (this.embeddedJettyServer != null) {
            this.embeddedJettyServer.shutdownImmediate();
        }
        if (this.jettyServer!= null) {
            this.jettyServer.setStopTimeout(0);
            this.jettyServer.setStopAtShutdown(false);
            try {
                this.jettyServer.stop();
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
                System.err.println("Failure to stop running jetty server, this may fail future tests.");
            }
        }
    }

    private ServletBasedInboundComPort createComPortMock() {
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        InboundComPortPool portPool = mock(InboundComPortPool.class);
        InboundDeviceProtocolPluggableClass pluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(EmbeddedJettyServer.MAX_IDLE_TIME, EmbeddedJettyServer.MAX_IDLE_TIME_DEFAULT_VALUE);
        when(comPort.getComPortPool()).thenReturn(portPool);
        when(((InboundComPortPool) portPool).getDiscoveryProtocolPluggableClass()).thenReturn(pluggableClass);
        when(pluggableClass.getProperties(Mockito.any(List.class))).thenReturn(typedProperties);
        return comPort;
    }

    @Test
    public void testInboundStartWithValidContextPath () {
        ServletBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("/embeddedJettyTest");
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWithNullContextPath () {
        ServletBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn(null);    // Note that business actually validates that the context path of a com port is not null
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWithEmptyContextPath () {
        ServletBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("");    // Note that business actually validates that the context path of a com port is not empty
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWithContextPathWithoutLeadingSlash () {
        ServletBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("embeddedJettyTest");
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWhenPortAlreadyInUse () {
        try {
            this.jettyServer = new Server(PORT_NUMBER);
            this.jettyServer.start();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            fail("Failed to start server on port " + PORT_NUMBER + " so testing that starting a second because the port is already in use does not make sense.");
        }
        ServletBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("/embeddedJettyTest");
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isNotEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundShutdown () {
        ServletBasedInboundComPort comPort = createComPortMock();
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdown();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testInboundShutdownImmediate () {
        ServletBasedInboundComPort comPort = createComPortMock();
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdownImmediate();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testEventsStart () throws URISyntaxException {
        this.embeddedJettyServer = EmbeddedJettyServer.newForEventMechanism(new URI("http://localhost:46000/remote/events"), this.embeddedJettyServerServiceProvider, eventAPIStatistics);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testEventsStartWhenPortAlreadyInUse () throws URISyntaxException {
        try {
            this.jettyServer = new Server(PORT_NUMBER);
            this.jettyServer.start();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            fail("Failed to start server on port " + PORT_NUMBER + " so testing that starting a second because the port is already in use does not make sense.");
        }
        this.embeddedJettyServer = EmbeddedJettyServer.newForEventMechanism(new URI("http://localhost:" + PORT_NUMBER + "/remote/events"), this.embeddedJettyServerServiceProvider, eventAPIStatistics);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isNotEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testEventsShutdown () throws URISyntaxException {
        this.embeddedJettyServer = EmbeddedJettyServer.newForEventMechanism(new URI("http://localhost:8082/remote/events"), this.embeddedJettyServerServiceProvider, eventAPIStatistics);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdown();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testEventsShutdownImmediate () throws URISyntaxException {
        this.embeddedJettyServer = EmbeddedJettyServer.newForEventMechanism(new URI("http://localhost:8083/remote/events"), this.embeddedJettyServerServiceProvider, eventAPIStatistics);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdownImmediate();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testQueriesStart () throws URISyntaxException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        String queryPostURI = "http://localhost:46000/remote/queries";
        when(comServer.getQueryApiPostUri()).thenReturn(queryPostURI);
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);
        this.embeddedJettyServer = EmbeddedJettyServer.newForQueryApi(new URI(queryPostURI), runningOnlineComServer, queryAPIStatistics);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testQueriesStartWhenPortAlreadyInUse () throws URISyntaxException {
        try {
            this.jettyServer = new Server(PORT_NUMBER);
            this.jettyServer.start();

            OnlineComServer comServer = mock(OnlineComServer.class);
            RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
            when(runningOnlineComServer.getComServer()).thenReturn(comServer);
            this.embeddedJettyServer = EmbeddedJettyServer.newForQueryApi(new URI("http://localhost:" + PORT_NUMBER + "/remote/queries"), runningOnlineComServer, queryAPIStatistics);

            // Business method
            this.embeddedJettyServer.start();

            // Asserts
            assertThat(this.embeddedJettyServer.getStatus()).isNotEqualTo(ServerProcessStatus.STARTED);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            fail("Failed to start server on port " + PORT_NUMBER + " so testing that starting a second because the port is already in use does not make sense.");
        }
    }

    @Test
    public void testQueriesShutdown () throws URISyntaxException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);
        this.embeddedJettyServer = EmbeddedJettyServer.newForQueryApi(new URI("http://localhost:8082/remote/queries"), runningOnlineComServer, queryAPIStatistics);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdown();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testQueriesShutdownImmediate () throws URISyntaxException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);
        this.embeddedJettyServer = EmbeddedJettyServer.newForQueryApi(new URI("http://localhost:8083/remote/queries"), runningOnlineComServer, queryAPIStatistics);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdownImmediate();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

}