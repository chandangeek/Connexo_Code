/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.coap;

import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;
import com.energyict.mdc.upl.TypedProperties;

import org.eclipse.californium.core.CoapServer;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link EmbeddedCaliforniumServer} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-19 (14:59)
 */
@RunWith(MockitoJUnitRunner.class)
public class EmbeddedCaliforniumServerTest {

    private static final int PORT_NUMBER = 9000;
    private static final String CONTEXT_PATH = "/embeddedCaliforniumTest";
    @Mock
    EventAPIStatistics eventAPIStatistics;
    @Mock
    QueryAPIStatistics queryAPIStatistics;
    @Mock
    private WebSocketEventPublisherFactory webSocketEventPublisherFactory;
    @Mock
    private InboundCommunicationHandler.ServiceProvider inboundCommunicationHandlerServiceProvider;
    @Mock
    private EmbeddedCaliforniumServer.ServiceProvider embeddedCaliforniumServerServiceProvider;
    @Mock
    private ComServerDAO comServerDAO;
    @Mock
    private RunningComServerImpl.ServiceProvider serviceProvider;

    private EmbeddedCaliforniumServer embeddedCaliforniumServer;
    private CoapServer coapServer;

    @Before
    public void setupServiceProvider() {
        when(this.embeddedCaliforniumServerServiceProvider.webSocketEventPublisherFactory()).thenReturn(this.webSocketEventPublisherFactory);
    }

    @After
    public void stopRunningCaliforniumServer() {
        if (this.embeddedCaliforniumServer != null) {
            this.embeddedCaliforniumServer.shutdownImmediate();
        }
        if (this.coapServer != null) {
            try {
                this.coapServer.stop();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.err.println("Failure to stop running jetty server, this may fail future tests.");
            }
        }
    }

    private CoapBasedInboundComPort createComPortMock() {
        CoapBasedInboundComPort comPort = mock(CoapBasedInboundComPort.class);
        InboundComPortPool portPool = mock(InboundComPortPool.class);
        InboundDeviceProtocolPluggableClass pluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        TypedProperties typedProperties = TypedProperties.empty();
        when(comPort.getComPortPool()).thenReturn(portPool);
        when(portPool.getDiscoveryProtocolPluggableClass()).thenReturn(pluggableClass);
        when(pluggableClass.getProperties(Mockito.any(List.class))).thenReturn(typedProperties);
        return comPort;
    }

    @Test
    public void testInboundStartWithValidContextPath() {
        CoapBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn(CONTEXT_PATH);
        this.embeddedCaliforniumServer = EmbeddedCaliforniumServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);

        // Business method
        this.embeddedCaliforniumServer.start();

        // Asserts
        assertThat(this.embeddedCaliforniumServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test(expected = NullPointerException.class)
    public void testInboundStartWithNullContextPath() {
        CoapBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn(null);    // Note that business actually validates that the context path of a com port is not null
        this.embeddedCaliforniumServer = EmbeddedCaliforniumServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);
    }

    @Test
    public void testInboundStartWithEmptyContextPath() {
        CoapBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("");
        this.embeddedCaliforniumServer = EmbeddedCaliforniumServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);
        // Business method
        this.embeddedCaliforniumServer.start();

        // Asserts
        assertThat(this.embeddedCaliforniumServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWithContextPathWithoutLeadingSlash() {
        CoapBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("embeddedCaliforniumTest");
        this.embeddedCaliforniumServer = EmbeddedCaliforniumServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);

        // Business method
        this.embeddedCaliforniumServer.start();

        // Asserts
        assertThat(this.embeddedCaliforniumServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWhenPortAlreadyInUse() {
        try {
            this.coapServer = new CoapServer(PORT_NUMBER);
            this.coapServer.start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            fail("Failed to start server on port " + PORT_NUMBER + " so testing that starting a second because the port is already in use does not make sense.");
        }
        CoapBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn(CONTEXT_PATH);
        this.embeddedCaliforniumServer = EmbeddedCaliforniumServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);

        // Business method
        this.embeddedCaliforniumServer.start();

        // Asserts
        assertThat(this.embeddedCaliforniumServer.getStatus()).isNotEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundShutdown() {
        CoapBasedInboundComPort comPort = createComPortMock();
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn(CONTEXT_PATH);
        this.embeddedCaliforniumServer = EmbeddedCaliforniumServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);
        this.embeddedCaliforniumServer.start();

        // Business method
        this.embeddedCaliforniumServer.shutdown();

        // Asserts
        assertThat(this.embeddedCaliforniumServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testInboundShutdownImmediate() {
        CoapBasedInboundComPort comPort = createComPortMock();
        when(comPort.getContextPath()).thenReturn(CONTEXT_PATH);
        this.embeddedCaliforniumServer = EmbeddedCaliforniumServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.inboundCommunicationHandlerServiceProvider);
        this.embeddedCaliforniumServer.start();

        // Business method
        this.embeddedCaliforniumServer.shutdownImmediate();

        // Asserts
        assertThat(this.embeddedCaliforniumServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }
}