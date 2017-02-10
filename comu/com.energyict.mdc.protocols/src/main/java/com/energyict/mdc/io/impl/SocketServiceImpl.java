package com.energyict.mdc.io.impl;

import com.energyict.mdc.channels.ip.datagrams.InboundUdpSessionImpl;
import com.energyict.mdc.channels.ip.socket.SocketComChannel;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.io.InboundUdpSession;
import com.energyict.mdc.upl.io.SocketService;
import com.energyict.mdc.upl.io.UPLSocketService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

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
@Component(name = "com.energyict.mdc.io.socketservice", service = {SocketService.class, UPLSocketService.class}, immediate = true)
public class SocketServiceImpl implements SocketService {

    @Activate
    public void activate() {
        Services.socketService(this);
    }

    @Deactivate
    public void deactivate() {
        Services.socketService(null);
    }

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