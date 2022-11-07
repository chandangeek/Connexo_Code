/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.factories;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.comserver.TCPBasedInboundComPort;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.coap.DefaultEmbeddedCoapServerFactory;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.CoapInboundComPortListener;
import com.energyict.mdc.engine.impl.core.ComChannelBasedComPortListenerImpl;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComServerThreadFactory;
import com.energyict.mdc.engine.impl.core.InboundComPortConnectorFactoryImpl;
import com.energyict.mdc.engine.impl.core.MultiThreadedComPortListener;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ServletInboundComPortListener;
import com.energyict.mdc.engine.impl.core.SingleThreadedComPortListener;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedJettyServer;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactoryImpl;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.io.SocketService;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComPortListenerFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (12:05)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComPortListenerFactoryImplTest {

    @Mock
    private RunningComServer runningComServer;
    @Mock
    private ComServer comServer;
    @Mock
    private SocketService socketService;
    @Mock
    private DeviceMessageService deviceMessageService;
    @Mock
    private ServerSocket serverSocket;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private ComChannelBasedComPortListenerImpl.ServiceProvider serviceProvider;
    @Mock
    private SerialComponentService serialComponentService;
    @Mock
    private HexService hexService;

    private Clock clock = Clock.systemDefaultZone();
    private ThreadFactory threadFactory;

    @Before
    public void initializeMocks() throws IOException {
        when(this.socketService.newTCPSocket(anyInt())).thenReturn(this.serverSocket);
        WebSocketEventPublisherFactoryImpl webSocketEventPublisherFactory =
                new WebSocketEventPublisherFactoryImpl(
                        this.runningComServer,
                        this.connectionTaskService,
                        this.communicationTaskService,
                        this.deviceService,
                        this.engineConfigurationService,
                        this.identificationService,
                        this.eventPublisher);
        when(this.serviceProvider.issueService()).thenReturn(mock(IssueService.class));
        when(this.serviceProvider.socketService()).thenReturn(this.socketService);
        when(this.serviceProvider.embeddedWebServerFactory()).thenReturn(new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory));
        when(this.serviceProvider.embeddedCoapServerFactory()).thenReturn(new DefaultEmbeddedCoapServerFactory(webSocketEventPublisherFactory));
        when(this.serviceProvider.threadFactory()).thenReturn(this.threadFactory);
        when(this.serviceProvider.eventPublisher()).thenReturn(this.eventPublisher);
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.inboundComPortConnectorFactory())
                .thenReturn(new InboundComPortConnectorFactoryImpl(
                        this.serialComponentService,
                        this.socketService,
                        this.hexService,
                        this.eventPublisher,
                        this.clock,
                        this.deviceMessageService
                ));
    }

    @Before
    public void setUpThreadFactory() {
        when(this.comServer.getName()).thenReturn("ComPortListenerFactoryImplTest");
        this.threadFactory = new ComServerThreadFactory(this.comServer);
    }

    @Before
    public void setupSocketService() {
    }

    @Test
    public void testWithActivePort() {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);

        // Busines method
        ComPortListener comPortListener = factory.newFor(runningComServer, this.activeComPort());

        // Asserts
        assertNotNull("Was NOT expecting the factory to return null for an active port", comPortListener);
    }

    @Test
    public void testWithInactivePort() {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);
        assertNull("Was expecting the factory to return null for an inactive port", factory.newFor(runningComServer, this.inactiveComPort()));
    }

    @Test
    public void testWithActivePortWithZeroSimultaneousConnections() {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);
        assertNull("Was expecting the factory to return null for active port with 0 simultaneous connections", factory.newFor(runningComServer, this.activeComPortWithZeroSimultaneousConnections()));
    }

    @Test
    public void testServletBasedInboundComPortListener() {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(runningComServer, this.servletBasedInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof ServletInboundComPortListener);
    }

    @Test
    public void testCoapBasedInboundComPortListener() {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(runningComServer, this.coapBasedInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof CoapInboundComPortListener);
    }

    @Test
    public void testSingleThreadedComPortListener() {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(runningComServer, this.singleThreadedInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof SingleThreadedComPortListener);
    }

    @Test
    public void testSingleThreadedTCPComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(runningComServer, this.singleThreadedTCPInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof SingleThreadedComPortListener);
    }

    @Test
    public void testMultiThreadedComPortListener() {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(runningComServer, this.multiThreadedInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof MultiThreadedComPortListener);
    }

    @Test
    public void testMultiThreadedComPortListenerForOneSimultaneousUDP(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(runningComServer, this.singleThreadedUDPInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof MultiThreadedComPortListener);
    }

    private ComServerDAO comServerDAO() {
        return mock(ComServerDAO.class);
    }

    private DeviceCommandExecutor deviceCommandExecutor() {
        return mock(DeviceCommandExecutor.class);
    }

    private InboundComPort activeComPort() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private InboundComPort inactiveComPort() {
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.isActive()).thenReturn(false);
        return comPort;
    }

    private InboundComPort activeComPortWithZeroSimultaneousConnections() {
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(0);
        return comPort;
    }

    private InboundComPort servletBasedInboundComPort() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        InboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(1));
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(true);
        InboundComPortPool portPool = mock(InboundComPortPool.class);
        InboundDeviceProtocolPluggableClass pluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(EmbeddedJettyServer.MAX_IDLE_TIME, EmbeddedJettyServer.MAX_IDLE_TIME_DEFAULT_VALUE);
        when(comPort.getComPortPool()).thenReturn(portPool);
        when(((InboundComPortPool) portPool).getDiscoveryProtocolPluggableClass()).thenReturn(pluggableClass);
        when(pluggableClass.getProperties(Mockito.any(List.class))).thenReturn(typedProperties);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(0);
        return comPort;
    }

    private InboundComPort coapBasedInboundComPort() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        CoapBasedInboundComPort comPort = mock(CoapBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(1));
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isCoapBased()).thenReturn(true);
        when(comPort.getContextPath()).thenReturn("test");
        InboundComPortPool portPool = mock(InboundComPortPool.class);
        InboundDeviceProtocolPluggableClass pluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(EmbeddedJettyServer.MAX_IDLE_TIME, EmbeddedJettyServer.MAX_IDLE_TIME_DEFAULT_VALUE);
        when(comPort.getComPortPool()).thenReturn(portPool);
        when(((InboundComPortPool) portPool).getDiscoveryProtocolPluggableClass()).thenReturn(pluggableClass);
        when(pluggableClass.getProperties(Mockito.any(List.class))).thenReturn(typedProperties);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(0);
        return comPort;
    }

    private InboundComPort singleThreadedInboundComPort() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isCoapBased()).thenReturn(false);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private InboundComPort singleThreadedUDPInboundComPort() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(false);
        when(comPort.isCoapBased()).thenReturn(false);
        when(comPort.isUDPBased()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private InboundComPort singleThreadedTCPInboundComPort() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(false);
        when(comPort.isCoapBased()).thenReturn(false);
        when(comPort.isTCPBased()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private InboundComPort multiThreadedInboundComPort() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(false);
        when(comPort.isCoapBased()).thenReturn(false);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(10000);
        return comPort;
    }

}