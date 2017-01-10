package com.energyict.mdc.io;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Provides services to create sockets.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-09 (11:12)
 */
public interface SocketService {

    /**
     * {@link ServerSocket#ServerSocket(int)}
     *
     * @param portNumber The port number
     * @return The newly created ServerSocket
     * @throws IOException Thrown by the ServerSocket constructor
     */
    ServerSocket newTCPSocket(int portNumber) throws IOException;

    /**
     * {@link DatagramSocket#DatagramSocket(int)}
     *
     * @param portNumber The port number
     * @return The newly created DatagramSocket
     * @throws SocketException Thrown by the DatagramSocket constructor
     */
    DatagramSocket newUDPSocket(int portNumber) throws SocketException;

}