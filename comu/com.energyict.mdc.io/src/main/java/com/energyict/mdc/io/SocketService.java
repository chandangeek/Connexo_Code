package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.protocol.ComChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Provides services to create sockets.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-13 (11:55)
 */
@ProviderType
public interface SocketService {

    /**
     * {@link ServerSocket#ServerSocket(int)}
     *
     * @param portNumber The port number
     * @return The newly created ServerSocket
     * @throws IOException Thrown by the ServerSocket constructor
     */
    ServerSocket newInboundTCPSocket(int portNumber) throws IOException;

    /**
     * {@link DatagramSocket#DatagramSocket(int)}
     *
     * @param portNumber The port number
     * @return The newly created DatagramSocket
     * @throws SocketException Thrown by the DatagramSocket constructor
     */
    DatagramSocket newInboundUDPSocket(int portNumber) throws SocketException;

    ComChannel newOutboundTcpIpConnection(String host, int port, int timeOut) throws IOException;

    ComChannel newOutboundUDPConnection(int bufferSize, String host, int port) throws IOException;

    InboundUdpSession newInboundUdpSession(int bufferSize, int port);

    ComChannel newSocketComChannel(Socket socket) throws IOException;

}