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

}