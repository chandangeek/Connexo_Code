package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;

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
    public ServerSocket newInboundTCPSocket(int portNumber) throws IOException;

    /**
     * {@link DatagramSocket#DatagramSocket(int)}
     *
     * @param portNumber The port number
     * @return The newly created DatagramSocket
     * @throws SocketException Thrown by the DatagramSocket constructor
     */
    public DatagramSocket newInboundUDPSocket(int portNumber) throws SocketException;

    public ComChannel newOutboundTcpIpConnection(String host, int port, int timeOut) throws IOException;

    public ComChannel newOutboundUDPConnection(int bufferSize, String host, int port) throws IOException;

    public InboundUdpSession newInboundUdpSession(int bufferSize, int port);

    public ComChannel newSocketComChannel (Socket socket) throws IOException;

}