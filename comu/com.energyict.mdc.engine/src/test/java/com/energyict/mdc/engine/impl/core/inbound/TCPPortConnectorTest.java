package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.InboundCommunicationException;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.services.HexService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Clock;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TCPPortConnector} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/12
 * Time: 14:43
 */
@RunWith(MockitoJUnitRunner.class)
public class TCPPortConnectorTest {

    public static final int PORT_NUMBER = 4096;
    public static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 2;

    @Mock
    private ComChannel comChannel;
    @Mock
    private HexService hexService;
    @Mock
    private SocketService socketService;
    @Mock
    private ServerSocket serverSocket;
    @Mock
    private EventPublisher eventPublisher;

    private Clock clock = Clock.systemUTC();

    @Before
    public void initializeMocksAndFactories() throws IOException {
        when(this.socketService.newSocketComChannel(any(Socket.class))).thenReturn(this.comChannel);
        when(this.socketService.newInboundTCPSocket(anyInt())).thenReturn(this.serverSocket);
    }

    private TCPBasedInboundComPort createTCPBasedInboundComPort() {
        TCPBasedInboundComPort tcpBasedInboundComPort = mock(TCPBasedInboundComPort.class);
        when(tcpBasedInboundComPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(tcpBasedInboundComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        return tcpBasedInboundComPort;
    }

    @Test(timeout = 2000)
    public void testProperAccept() throws IOException {
        Socket socket = mock(Socket.class);
        when(this.serverSocket.accept()).thenReturn(socket);

        TCPBasedInboundComPort tcpBasedInboundComPort = createTCPBasedInboundComPort();

        // creating a Connector with a ServerSocket with a backLog of 2 sessions
        TCPPortConnector connector = new TCPPortConnector(tcpBasedInboundComPort, socketService, this.hexService, this.eventPublisher, this.clock);

        // business method
        ComChannel accept = connector.accept();

        // asserts
        assertThat(accept).isNotNull();
        assertThat(accept).isInstanceOf(ComPortRelatedComChannel.class);
    }

    @Test(timeout = 1000, expected = InboundCommunicationException.class)
    public void testConstructorFailure() throws IOException, InboundCommunicationException {
        doThrow(new IOException("Something fishy happened for testing purposes")).
                when(this.socketService).
                newInboundTCPSocket(anyInt());

        TCPBasedInboundComPort tcpBasedInboundComPort = createTCPBasedInboundComPort();

        try {
            // Business method
            new TCPPortConnector(tcpBasedInboundComPort, socketService, this.hexService, this.eventPublisher, this.clock);
        }
        catch (InboundCommunicationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION)) {
                fail("Message should have indicated that their was an exception during the setup of the inbound call, but was " + e.getMessage());
            }
            else {
                throw e;
            }
        }
    }

    @Test(timeout = 2000, expected = InboundCommunicationException.class)
    public void testAcceptFailure() throws IOException, InboundCommunicationException {
        when(this.serverSocket.accept()).thenThrow(new IOException("Something fishy happened during the accept for testing purposes"));

        TCPBasedInboundComPort tcpBasedInboundComPort = createTCPBasedInboundComPort();

        // creating a Connector with a ServerSocket with a backLog of 2 sessions
        TCPPortConnector connector = new TCPPortConnector(tcpBasedInboundComPort, socketService, this.hexService, this.eventPublisher, this.clock);

        try {
            // Business method
            connector.accept();
        }
        catch (InboundCommunicationException e) {
            if (!e.getMessageSeed().equals(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION)) {
                fail("Message should have indicated that their was an exception during the setup of the inbound call, but was " + e.getMessage());
            }
            else {
                throw e;
            }
        }
    }

}