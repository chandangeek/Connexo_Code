//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

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

    private void udpRead() throws IOException {
        if (super.available() <= 0) {
            synchronized(this.datagramSocket) {
                int soTimeout = this.datagramSocket.getSoTimeout();
                this.receiveData = new byte[this.bufferSize];
                DatagramPacket datagramPacket = this.socketAddress != null ? this.cleanDatagramPacket() : new DatagramPacket(this.receiveData, this.receiveData.length);
                this.datagramSocket.setSoTimeout(100);

                label73: {
                    try {
                        this.datagramSocket.receive(datagramPacket);
                        break label73;
                    } catch (Throwable var10) {
                    } finally {
                        this.datagramSocket.setSoTimeout(soTimeout);
                    }

                    return;
                }

                this.write(this.receiveData, datagramPacket.getOffset(), datagramPacket.getLength());
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
