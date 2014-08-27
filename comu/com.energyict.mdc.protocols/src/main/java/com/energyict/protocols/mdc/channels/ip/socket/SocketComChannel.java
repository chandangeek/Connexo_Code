package com.energyict.protocols.mdc.channels.ip.socket;

import com.energyict.protocols.mdc.channels.SynchroneousComChannel;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;

import java.io.IOException;
import java.net.Socket;

/**
 * Creates a simple {@link ComChannel}
 * wrapped around a {@link Socket}
 *
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 16:33
 */
public class SocketComChannel extends SynchroneousComChannel {

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
            } finally {
                if (this.socket != null) {
                    this.socket.close();
                }
            }
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

}