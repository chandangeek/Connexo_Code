package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.VirtualUdpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * The abstract UDP session handles the Input- and OutputStream as well as the common components.
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:57
 */
public abstract class AbstractUdpSession implements VirtualUdpSession {

    private final int bufferSize;

    private DatagramSocket datagramSocket;
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

    @Override
    public void close() throws IOException {
        try (InputStream is = this.inputStream; OutputStream os = this.outputStream) {
            if (this.datagramSocket != null) {
                this.datagramSocket.close();
            }
        }
    }

    /**
     * A DatagramInputStream is a <i>virtual</i> inputStream for an UDP <i>session</i>.
     * UDP sessions are stateless, so actually there is no existence of a <i>stream</i>.
     * The DatagramInputStream will use the {@link java.net.DatagramSocket} and {@link java.net.SocketAddress}
     * from the initial accept to hold a session for this inputStream. If data is received
     * from a {@link java.net.DatagramPacket}, we write it to the pipe, so readers of this inputStream
     * get {@link #available()} bytes when they try to read.
     */
    protected class DatagramInputStream extends PipedInputStream {

        private final PipedOutputStream pipedOutputStream;
        private byte[] receiveData = new byte[0];

        protected DatagramInputStream(PipedOutputStream src, int pipeSize) throws IOException {
            super(src, pipeSize); // need to give the pipeSize in the constructor, otherwise it is not worth it
            pipedOutputStream = src;
        }

        /**
         * Check if currently we have data available on the pipedInputStream.
         * If data is available, then don't add any additional data.
         * If no data is available, then wait for an incoming UDP packet and add it to the pipe.
         *
         * @throws IOException if a connection related exception occurs
         */
        private void udpRead() throws IOException {
            if (available() <= 0) {
                this.receiveData = new byte[bufferSize];
                DatagramPacket datagramPacket = cleanDatagramPacket();
                datagramSocket.receive(datagramPacket);
                write(receiveData, datagramPacket.getOffset(), datagramPacket.getLength());
            }
        }

        @Override
        public synchronized int read() throws IOException {
            udpRead();
            return super.read();
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            udpRead();
            return super.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            udpRead();
            return super.read(b);
        }

        protected void write(byte[] data, int off, int len) throws IOException {
            this.pipedOutputStream.write(data, off, len);
        }

        private DatagramPacket cleanDatagramPacket() throws SocketException {
            return new DatagramPacket(receiveData, receiveData.length, socketAddress);
        }

    }

    /**
     * A DatagramOutputStream is a <i>virtual</i> outputStream for an UDP <i>session</i>.
     * UDP sessions are stateless, so actually there is no existence of a <i>stream</i>.
     * The DatagramOutputStream will use the {@link java.net.DatagramSocket} and {@link java.net.SocketAddress}
     * from the initial accept to hold a session for this outputStream. If data needs
     * to be written, we simple create a DatagramPacket for it and send it over via
     * the datagramSocket.
     */
    protected class DatagramOutputStream extends OutputStream {

        /**
         * ByteBuffer to collect the bytes when the {@link #write(int)} method is used.
         */
        private ByteBuffer dataToSend = ByteBuffer.allocate(bufferSize);

        public DatagramOutputStream() {
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
            datagramSocket.send(new DatagramPacket(b, off, len, socketAddress));
        }

        @Override
        public void write(byte[] b) throws IOException {
            datagramSocket.send(new DatagramPacket(b, 0, b.length, socketAddress));
        }

        @Override
        public void flush() throws IOException {
            if (dataToSend.position() > 0) {
                datagramSocket.send(new DatagramPacket(dataToSend.array(), 0, dataToSend.position(), socketAddress));
                super.flush();
                // reset the ByteBuffer so we can fill him up again.
                dataToSend = ByteBuffer.allocate(bufferSize);
            }
        }
    }
}
