package com.energyict.mdc.channel.ip.socket;

import com.energyict.mdc.channel.SynchroneousComChannel;
import com.energyict.mdc.channel.ip.datagrams.MessageSeeds;
import com.energyict.mdc.protocol.ComChannelRemoteAddress;
import com.energyict.mdc.protocol.ComChannelType;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;
import java.net.Socket;

/**
 * Creates a simple {@link com.energyict.mdc.protocol.ComChannel}
 * wrapped around a {@link Socket}
 * <p/>
 *
 * Date: 17/08/12
 * Time: 16:33
 */
public class SocketComChannel extends SynchroneousComChannel implements ComChannelRemoteAddress {

    private final Socket socket;

    /**
     * Creates a new SocketComChannel that uses the specified
     * InputStream and OutputStream as underlying communication mechanisms.
     * The ComChannel is open for writing.
     * <p/>
     * Note that any read call is infinitely blocking, by default.
     * You can adjust this behaviour by calling {@link SocketComChannel#setTimeout(long)}
     * and setting a value that is greater than 0.
     *
     * @param socket the used Socket for the ComChannel
     */
    public SocketComChannel(Socket socket) throws IOException {
        super(socket.getInputStream(), socket.getOutputStream());
        this.socket = socket;
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.SocketComChannel;
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
            throw new ConnectionCommunicationException( e, MessageSeeds.UNEXPECTED_IO_EXCEPTION);
        }
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
            throw new ConnectionCommunicationException(e, MessageSeeds.UNEXPECTED_IO_EXCEPTION);
        }
    }

    @Override
    public String getRemoteSocketAddress(){
        return this.socket.getRemoteSocketAddress().toString();
    }

}