package com.energyict.protocols.mdc.services;

import com.energyict.mdc.protocol.api.ComChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Provices services to create sockets.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-13 (11:55)
 */
public interface SocketService {

    /**
     * {@link ServerSocket#ServerSocket(int)}
     *
     * @param portNumber The port number
     * @return The newly created ServerSocket
     * @throws IOException Thrown by the ServerSocket constructor
     */
    public ServerSocket newTCPSocket (int portNumber) throws IOException;

    /**
     * {@link DatagramSocket#DatagramSocket(int)}
     *
     * @param portNumber The port number
     * @return The newly created DatagramSocket
     * @throws IOException Thrown by the DatagramSocket constructor
     */
    public DatagramSocket newUDPSocket (int portNumber) throws SocketException;

    public ComChannel newSocketComChannel (Socket socket) throws IOException;

}