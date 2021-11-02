package com.energyict.mdc.channel.ip.datagrams;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class OutboundDatagramInputStream extends DatagramInputStream {
    private static final int SHORT_TIMEOUT = 100;
    private final DatagramSocket datagramSocket;
    private final SocketAddress socketAddress;
    private final int bufferSize;
    private byte[] receiveData = new byte[0];

    protected OutboundDatagramInputStream(DatagramSocket datagramSocket, SocketAddress socketAddress, int bufferSize, PipedOutputStream src, int pipeSize) throws IOException {
        super(src, pipeSize);
        this.datagramSocket = datagramSocket;
        this.socketAddress = socketAddress;
        this.bufferSize = bufferSize;
    }

    public synchronized int available() throws IOException {
        this.udpRead();
        return super.available();
    }

    /**
     * Check if currently we have data available on the pipedInputStream.
     * If data is available, then don't add any additional data.
     * If no data is available, then wait for an incoming UDP packet and add it to the pipe.
     *
     * @throws IOException if a connection related exception occurs
     */
    private void udpRead() throws IOException {
        if (super.available() <= 0) {
            synchronized (datagramSocket) {
                int soTimeout = datagramSocket.getSoTimeout();
                this.receiveData = new byte[bufferSize];
                DatagramPacket datagramPacket = socketAddress != null ? cleanDatagramPacket() :
                        new DatagramPacket(receiveData, receiveData.length);
                datagramSocket.setSoTimeout(SHORT_TIMEOUT);
                try {
                    datagramSocket.receive(datagramPacket);
                } catch (Throwable e) {
                    return;//No data for now, OK, let's move on
                } finally {
                    datagramSocket.setSoTimeout(soTimeout);
                }
                write(receiveData, datagramPacket.getOffset(), datagramPacket.getLength());
            }
        }
    }

    public synchronized int read() throws IOException {
        this.udpRead();
        return super.read();
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        this.udpRead();
        return super.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        this.udpRead();
        return super.read(b);
    }

    protected void write(byte[] data, int off, int len) throws IOException {
        this.pipedOutputStream.write(data, off, len);
    }

    private DatagramPacket cleanDatagramPacket() throws SocketException {
        return new DatagramPacket(this.receiveData, this.receiveData.length, this.socketAddress);
    }
}
