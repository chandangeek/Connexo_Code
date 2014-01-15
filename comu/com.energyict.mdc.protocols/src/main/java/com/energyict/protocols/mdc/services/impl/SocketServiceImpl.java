package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.protocols.mdc.channels.ip.socket.SocketComChannel;
import com.energyict.protocols.mdc.services.SocketService;
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
@Component(name = "com.energyict.protocols.mdc.services.socketservice", service = SocketService.class)
public class SocketServiceImpl implements SocketService {

    @Override
    public ServerSocket newTCPSocket (int portNumber) throws IOException {
        return new ServerSocket(portNumber);
    }

    @Override
    public DatagramSocket newUDPSocket (int portNumber) throws SocketException {
        return new DatagramSocket(portNumber);
    }

    @Override
    public ComChannel newSocketComChannel(Socket socket) throws IOException {
        return new SocketComChannel(socket);
    }

}