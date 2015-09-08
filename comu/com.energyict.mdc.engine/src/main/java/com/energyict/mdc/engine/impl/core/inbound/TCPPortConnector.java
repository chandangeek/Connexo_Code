package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.io.InboundCommunicationException;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.services.HexService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Clock;

/**
 * Implementation of an {@link InboundComPortConnector} for a {@link ComPort} of the type {@link ComPortType#TCP}.
 * <p>
 * Copyrights EnergyICT
 * Date: 18/10/12
 * Time: 13:49
 */
public class TCPPortConnector implements InboundComPortConnector {

    /**
     * The ServerSocket which will be used to accept incoming sessions and create a Socket for them.
     */
    private final ServerSocket serverSocket;
    private final SocketService socketService;
    private final HexService hexService;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final InboundComPort comPort;

    public TCPPortConnector(TCPBasedInboundComPort comPort, SocketService socketService, HexService hexService, EventPublisher eventPublisher, Clock clock) {
        super();
        this.comPort = comPort;
        this.hexService = hexService;
        this.socketService = socketService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        try {
            this.serverSocket = socketService.newInboundTCPSocket(comPort.getPortNumber());
        }
        catch (IOException e) {
            throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
        }
    }

    @Override
    public ComPortRelatedComChannel accept() {
        try {
            final Socket socket = this.serverSocket.accept();
            return new ComPortRelatedComChannelImpl(this.getSocketService().newSocketComChannel(socket), this.comPort, this.clock, this.hexService, eventPublisher);
        }
        catch (IOException e) {
            throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
        }
    }

    @Override
    public void close() {
        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            }
            catch (IOException e) {
                throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
            }
        }
    }

    protected SocketService getSocketService() {
        return this.socketService;
    }

}