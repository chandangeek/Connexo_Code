package com.energyict.mdc.channel.ip.datagrams;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * A DatagramOutputStream is a <i>virtual</i> outputStream for an UDP <i>session</i>.
 * UDP sessions are stateless, so actually there is no existence of a <i>stream</i>.
 * The DatagramOutputStream will use the {@link DatagramSocket} and {@link SocketAddress}
 * from the initial accept to hold a session for this outputStream. If data needs
 * to be written, we simple create a DatagramPacket for it and send it over via
 * the datagramSocket.
 */
public class DatagramOutputStream extends OutputStream {

    /**
     * ByteBuffer to collect the bytes when the {@link #write(int)} methodprivate final  is used.
     */
    private ByteBuffer dataToSend;

    private final DatagramSocket datagramSocket;
    private final SocketAddress remoteSocketAddress;
    private final int bufferSize;

    public DatagramOutputStream(DatagramSocket datagramSocket, SocketAddress socketAddress, int bufferSize) {
        this.datagramSocket = datagramSocket;
        this.remoteSocketAddress = socketAddress;
        this.bufferSize = bufferSize;
        dataToSend = ByteBuffer.allocate(bufferSize);
    }

    /**
     * This is the most inefficient write operation for an <i>UDP session</i>.
     * We advise you to use the far more usable {@link #write(byte[])} or
     * {@link #write(byte[], int, int)}.
     * <p/>
     * This write method will buffer all given bytes (or ints in this case) into a ByteBuffer.
     * <b>NO DATA WILL BE TRANSFERRED</b> until you call the {@link #flush()} method.
     *
     * @param b data to add to the ByteBuffer
     * @throws IOException will not occur in this case
     */
    @Override
    public void write(int b) throws IOException {
        dataToSend.put((byte) b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        datagramSocket.send(new DatagramPacket(b, off, len, remoteSocketAddress));
    }

    @Override
    public void write(byte[] b) throws IOException {
        datagramSocket.send(new DatagramPacket(b, 0, b.length, remoteSocketAddress));
    }

    @Override
    public void flush() throws IOException {
        if (dataToSend.position() > 0) {
            datagramSocket.send(new DatagramPacket(dataToSend.array(), 0, dataToSend.position(), remoteSocketAddress));
            super.flush();
            // reset the ByteBuffer so we can fill him up again.
            dataToSend = ByteBuffer.allocate(bufferSize);
        }
    }
}

