package com.energyict.mdc.io.impl;

import com.energyict.mdc.channels.ip.datagrams.InboundUdpSessionImpl;
import com.energyict.mdc.channels.ip.socket.SocketComChannel;
import com.energyict.mdc.io.InboundUdpSession;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.ComChannel;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Provides an implementation for the {@link SocketService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-13 (11:56)
 */
@Component(name = "com.energyict.mdc.io.socketservice", service = SocketService.class)
public class SocketServiceImpl implements SocketService {

    @Override
    public ServerSocket newTCPSocket(int portNumber) throws IOException {
        return new ServerSocket(portNumber);
    }

    @Override
    public InboundUdpSession newInboundUdpSession(int bufferSize, int port) {
        return new InboundUdpSessionImpl(bufferSize, port, this);
    }

    @Override
    public ComChannel newSocketComChannel(Socket socket) throws IOException {
        return new SocketComChannel(socket);
    }

    @Override
    public DatagramSocket newUDPSocket(int portNumber) throws SocketException {
        return new DatagramSocket(portNumber);
    }
}