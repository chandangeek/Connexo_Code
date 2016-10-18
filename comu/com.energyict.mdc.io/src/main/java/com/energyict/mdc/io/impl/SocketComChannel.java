package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.ConnectionCommunicationException;

import java.io.IOException;
import java.net.Socket;

/**
 * Creates a simple {@link ComChannel} wrapped around a {@link Socket}.
 * <p>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 16:33
 */
public class SocketComChannel extends SynchronousComChannel {

    private final Socket socket;

    /**
     * Creates a new SocketComChannel that uses the specified
     * InputStream and OutputStream as underlying communication mechanisms.
     * The ComChannel is open for writing.
     *
     * @param socket the used Socket for the ComChannel
     */
    public SocketComChannel(Socket socket) throws IOException {
        super(socket.getInputStream(), socket.getOutputStream());
        this.socket = socket;
    }

    @Override
    public void doClose() {
        try {
            try {
                super.doClose();
            }
            finally {
                this.socket.close();
            }
        }
        catch (IOException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.SOCKET_COM_CHANNEL;
    }

    /**
     * Specific implementation, using the given value as the socket timeout.
     * Reading calls on the underlying inputstream are no longer infinite.
     * <p/>
     * This also means that the protocol implementations that use this SocketComChannel no longer have to manually poll every x millis to
     * check if bytes are available on the inputstream. They can just call the reading method. If no bytes become available
     * after x millis, a timeout exception will be thrown.
     * <p/>
     * This mechanism is "interrupt driven" and replaces frequent polling. This way, resources (CPU) are used more efficiently.
     *
     * @param millis the time in milliseconds that the protocol implementation waits for a response from the device
     */
    @Override
    public void setTimeout(long millis) {
        try {
            socket.setSoTimeout((int) millis);
        } catch (IOException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }
}