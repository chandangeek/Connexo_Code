package com.energyict.mdc.channel.ip.datagrams;

import com.energyict.mdc.upl.io.VirtualUdpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * The abstract UDP session handles the Input- and OutputStream as well as the common components.
 * <p>
 *
 * Date: 9/11/12
 * Time: 10:57
 */
public abstract class AbstractUdpSession implements VirtualUdpSession {
    public static final Logger logger = Logger.getLogger(AbstractUdpSession.class.getName());

    protected static final Map<String, DatagramComChannel> sessions = new ConcurrentHashMap<>();

    private final int bufferSize;

    protected DatagramSocket datagramSocket;
    private SocketAddress socketAddress;
    private InputStream inputStream;
    private OutputStream outputStream;

    protected AbstractUdpSession(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public void setSocketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public DatagramSocket getDatagramSocket() {
        return this.datagramSocket;
    }

    protected int getBufferSize() {
        return this.bufferSize;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    protected void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    protected void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void closeComChannel(String remoteAddress) {
        if (sessions.remove(remoteAddress) != null) {
            logger.info("session for " + remoteAddress + " removed; #sessions=" + sessions.size());
        }
    }

    @Override
    public void close() throws IOException {
        try (InputStream is = this.inputStream; OutputStream os = this.outputStream) {
            if (this.datagramSocket != null) {
                this.datagramSocket.close();
            }
        }
    }
}
