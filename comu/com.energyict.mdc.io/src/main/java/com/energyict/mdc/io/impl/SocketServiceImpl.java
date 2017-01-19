package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.InboundUdpSession;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.ComChannel;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
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
    public ServerSocket newInboundTCPSocket(int portNumber) throws IOException {
        return new ServerSocket(portNumber);
    }

    @Override
    public DatagramSocket newInboundUDPSocket(int portNumber) throws SocketException {
        return new DatagramSocket(portNumber);
    }

    @Override
    public ComChannel newOutboundTcpIpConnection(String host, int port, int timeOut) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeOut);
        return new SocketComChannel(socket);
    }

    @Override
    public ComChannel newOutboundUDPConnection(int bufferSize, String host, int port) throws IOException {
        OutboundUdpSession udpSession = new OutboundUdpSession(bufferSize, host, port);
        return new DatagramComChannel(udpSession);
    }

    @Override
    public InboundUdpSession newInboundUdpSession(int bufferSize, int port) {
        return new InboundUdpSessionImpl(bufferSize, port, this);
    }

    @Override
    public ComChannel newSocketComChannel(Socket socket) throws IOException {
        return new SocketComChannel(socket);
    }

}