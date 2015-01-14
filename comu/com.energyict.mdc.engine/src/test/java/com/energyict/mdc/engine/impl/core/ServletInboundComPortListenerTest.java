package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundCapableComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedJettyServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactoryImpl;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.elster.jupiter.time.TimeDuration;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.mdc.engine.impl.core.ServletInboundComPortListener} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 8:51
 */
@RunWith(MockitoJUnitRunner.class)
public class ServletInboundComPortListenerTest {

    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 3;
    private static final TimeDuration INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.SECONDS);

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private InboundCommunicationHandler.ServiceProvider inboundCommunicationHandlerServiceProvider;

    @Before
    public void setupServiceProvider () {
        WebSocketEventPublisherFactoryImpl webSocketEventPublisherFactory =
                new WebSocketEventPublisherFactoryImpl(
                        this.connectionTaskService,
                        this.communicationTaskService,
                        this.deviceService,
                        this.engineConfigurationService,
                        this.identificationService,
                        this.eventPublisher);
        when(this.inboundCommunicationHandlerServiceProvider.embeddedWebServerFactory()).thenReturn(new DefaultEmbeddedWebServerFactory(webSocketEventPublisherFactory));
        when(this.inboundCommunicationHandlerServiceProvider.eventPublisher()).thenReturn(this.eventPublisher);
    }

    @Test(timeout = 10000)
    public void testNewComPortIsReturned() throws BusinessException {
        final InboundComPort inboundComPort = mockComPort("originalComPort");
        when(inboundComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);

        ServletInboundComPortListener servletInboundComPortListener = new ServletInboundComPortListener(inboundComPort, comServerDAO, deviceCommandExecutor, this.inboundCommunicationHandlerServiceProvider);

        int addedCapacity = 10;
        InboundComPort newComPort = mockComPort("newComPort");
        when(newComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + addedCapacity);

        // Business method
        InboundComPort finalComPort = servletInboundComPortListener.applyChanges(newComPort, inboundComPort);

        // Asserts
        assertThat(finalComPort).isEqualTo(newComPort);
    }

    @Test
    public void testApplyChanges() throws Exception {
        final EmbeddedJettyServer mockedJetty = mock(EmbeddedJettyServer.class);
        final InboundComPort inboundComPort = mockComPort("originalComPort");
        when(inboundComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);

        ServletInboundComPortListener servletInboundComPortListener = new ServletInboundComPortListener(inboundComPort, comServerDAO, deviceCommandExecutor, this.inboundCommunicationHandlerServiceProvider);

        final ServletBasedInboundComPort newComPort = mockComPort("newComPort");
        when(newComPort.getPortNumber()).thenReturn(80);

        EmbeddedWebServerFactory embeddedWebServerFactory = mock(EmbeddedWebServerFactory.class);
        when(embeddedWebServerFactory.
                findOrCreateFor(
                    any(ServletBasedInboundComPort.class),
                    any(ComServerDAO.class),
                    any(DeviceCommandExecutor.class), eq(this.inboundCommunicationHandlerServiceProvider))).
        thenReturn(mockedJetty);
        servletInboundComPortListener.applyChanges(newComPort, inboundComPort);
    }

    private ServletBasedInboundComPort mockComPort(String name) {
        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getChangesInterPollDelay()).thenReturn(INTER_POLL_DELAY);
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getName()).thenReturn("ServletComPortListener#" + name);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

}
