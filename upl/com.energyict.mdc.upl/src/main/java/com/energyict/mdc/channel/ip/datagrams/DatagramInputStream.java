package com.energyict.mdc.channel.ip.datagrams;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Logger;

/**
 * A DatagramInputStream is a <i>virtual</i> inputStream for an UDP <i>session</i>.
 * UDP sessions are stateless, so actually there is no existence of a <i>stream</i>.
 * The DatagramInputStream will use the {@link java.net.DatagramSocket} and {@link java.net.SocketAddress}
 * from the initial accept to hold a session for this inputStream. If data is received
 * from a {@link java.net.DatagramPacket}, we write it to the pipe, so readers of this inputStream
 * get {@link #available()} bytes when they try to read.
 */
public class DatagramInputStream extends PipedInputStream {
    public static final Logger logger = Logger.getLogger(DatagramInputStream.class.getName());
    protected final PipedOutputStream pipedOutputStream;

    protected DatagramInputStream(PipedOutputStream src, int pipeSize) throws IOException {
        super(src, pipeSize); // need to give the pipeSize in the constructor, otherwise it is not worth it
        pipedOutputStream = src;
    }

    protected void write(byte[] data, int off, int len) throws IOException {
        pipedOutputStream.write(data, off, len);
        logger.info("written " + len + " bytes to DatagramInputStream");
    }
}