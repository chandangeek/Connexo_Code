package com.energyict.mdc.engine.impl.core;

import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServletInboundComPortListener;
import com.energyict.mdc.engine.impl.web.EmbeddedJettyServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundCapableComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.issues.IssueService;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    private static final TimeDuration INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.SECONDS);

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;

    @Test(timeout = 10000)
    public void testNewComPortIsReturned() throws BusinessException {
        final InboundComPort inboundComPort = mockComPort("originalComPort");
        when(inboundComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        final ComServerDAO comServerDAO = mock(ComServerDAO.class);

        ServletInboundComPortListener servletInboundComPortListener = new ServletInboundComPortListener(inboundComPort, comServerDAO, deviceCommandExecutor, mock(IssueService.class));

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

        ServletInboundComPortListener servletInboundComPortListener = new ServletInboundComPortListener(inboundComPort, comServerDAO, deviceCommandExecutor, mock(IssueService.class));

        final ServletBasedInboundComPort newComPort = mockComPort("newComPort");
        when(newComPort.getPortNumber()).thenReturn(80);

        EmbeddedWebServerFactory embeddedWebServerFactory = mock(EmbeddedWebServerFactory.class);
        when(embeddedWebServerFactory.
                findOrCreateFor(
                    any(ServletBasedInboundComPort.class),
                    any(ComServerDAO.class),
                    any(DeviceCommandExecutor.class), mock(IssueService.class))).
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
