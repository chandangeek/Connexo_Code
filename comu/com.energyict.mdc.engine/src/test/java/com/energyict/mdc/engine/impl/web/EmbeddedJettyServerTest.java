package com.energyict.mdc.engine.impl.web;

import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;

import org.eclipse.jetty.server.Server;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
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

    private EmbeddedJettyServer embeddedJettyServer;
    private Server jettyServer;
    private FakeServiceProvider serviceProvider;

    @After
    public void stopRunningJettyServer () {
        if (this.embeddedJettyServer != null) {
            this.embeddedJettyServer.shutdownImmediate();
        }
        if (this.jettyServer!= null) {
            this.jettyServer.setGracefulShutdown(0);
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

    @Test
    public void testInboundStartWithValidContextPath () {
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("/embeddedJettyTest");
        serviceProvider = new FakeServiceProvider();
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.serviceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWithNullContextPath () {
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn(null);    // Note that business actually validates that the context path of a com port is not null
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.serviceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWithEmptyContextPath () {
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("");    // Note that business actually validates that the context path of a com port is not empty
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.serviceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundStartWithContextPathWithoutLeadingSlash () {
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("embeddedJettyTest");
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.serviceProvider);

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
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(comPort.getContextPath()).thenReturn("/embeddedJettyTest");
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.serviceProvider);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isNotEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testInboundShutdown () {
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.serviceProvider);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdown();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testInboundShutdownImmediate () {
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        this.embeddedJettyServer = EmbeddedJettyServer.newForInboundDeviceCommunication(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.serviceProvider);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdownImmediate();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testEventsStart () throws URISyntaxException {
        this.embeddedJettyServer = EmbeddedJettyServer.newForEventMechanism(new URI("http://localhost:8081/remote/events"));

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
        this.embeddedJettyServer = EmbeddedJettyServer.newForEventMechanism(new URI("http://localhost:" + PORT_NUMBER + "/remote/events"));

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isNotEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testEventsShutdown () throws URISyntaxException {
        this.embeddedJettyServer = EmbeddedJettyServer.newForEventMechanism(new URI("http://localhost:8082/remote/events"));
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdown();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testEventsShutdownImmediate () throws URISyntaxException {
        this.embeddedJettyServer = EmbeddedJettyServer.newForEventMechanism(new URI("http://localhost:8083/remote/events"));
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdownImmediate();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testQueriesStart () throws URISyntaxException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        String queryPostURI = "http://localhost:8081/remote/queries";
        when(comServer.getQueryApiPostUri()).thenReturn(queryPostURI);
        this.embeddedJettyServer = EmbeddedJettyServer.newForQueryApi(new URI(queryPostURI), comServer);

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
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            fail("Failed to start server on port " + PORT_NUMBER + " so testing that starting a second because the port is already in use does not make sense.");
        }
        OnlineComServer comServer = mock(OnlineComServer.class);
        this.embeddedJettyServer = EmbeddedJettyServer.newForQueryApi(new URI("http://localhost:" + PORT_NUMBER + "/remote/queries"), comServer);

        // Business method
        this.embeddedJettyServer.start();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isNotEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testQueriesShutdown () throws URISyntaxException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        this.embeddedJettyServer = EmbeddedJettyServer.newForQueryApi(new URI("http://localhost:8082/remote/queries"), comServer);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdown();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testQueriesShutdownImmediate () throws URISyntaxException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        this.embeddedJettyServer = EmbeddedJettyServer.newForQueryApi(new URI("http://localhost:8083/remote/queries"), comServer);
        this.embeddedJettyServer.start();

        // Business method
        this.embeddedJettyServer.shutdownImmediate();

        // Asserts
        assertThat(this.embeddedJettyServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

}