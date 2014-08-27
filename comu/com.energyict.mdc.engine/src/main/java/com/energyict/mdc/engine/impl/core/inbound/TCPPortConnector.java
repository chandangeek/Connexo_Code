package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.exceptions.InboundCommunicationException;
import com.energyict.mdc.protocol.api.services.HexService;

import com.energyict.protocols.mdc.services.SocketService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Implementation of an {@link InboundComPortConnector} for a {@link ComPort} of the type {@link ComPortType#TCP}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/12
 * Time: 13:49
 */
public class TCPPortConnector implements InboundComPortConnector {

    /**
     * The ServerSocket which will be used to accept incoming sessions and create a Socket for them
     */
    private final ServerSocket serverSocket;
    private final SocketService socketService;
    private final HexService hexService;

    public TCPPortConnector(TCPBasedInboundComPort comPort, SocketService socketService, HexService hexService) {
        this.hexService = hexService;
        this.socketService = socketService;
        try {
            this.serverSocket = socketService.newTCPSocket(comPort.getPortNumber());
        }
        catch (IOException e) {
            throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
        }
    }

    @Override
    public ComPortRelatedComChannel accept() {
        try {
            final Socket socket = this.serverSocket.accept();
            return new ComPortRelatedComChannelImpl(this.getSocketService().newSocketComChannel(socket), this.hexService);
        }
        catch (IOException e) {
            throw new InboundCommunicationException(MessageSeeds.UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION, e);
        }
    }

    protected SocketService getSocketService() {
        return this.socketService;
    }

}