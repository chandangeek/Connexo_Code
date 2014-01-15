package com.energyict.dialer.core.impl;

import com.energyict.mdc.protocol.api.dialer.core.UDPSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPSessionImpl implements UDPSession {

    private static final Logger LOGGER = Logger.getLogger(UDPSessionImpl.class.getName());

    DatagramPacket receivePacket;
    ServerDatagramConnection serverDatagramConnection;
    String signature;

    DatagramInputStream consumeInputStream = null;
    DatagramOutputStream consumeOutputStream = null;

    public UDPSessionImpl(ServerDatagramConnection serverDatagramConnection, String signature) throws IOException {
        this.serverDatagramConnection = serverDatagramConnection;
        this.signature = signature;
        consumeInputStream = new DatagramInputStream(new PipedOutputStream());
        consumeOutputStream = new DatagramOutputStream(this);
    }


    @Override
    public void receive(DatagramPacket receivePacket) throws IOException {
        this.receivePacket = receivePacket;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("UDPSession " + signature + " received " + receivePacket.getLength() + " bytes of data...");
        }
        consumeInputStream.write(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
    }

    @Override
    public void close() {
        try {
            if (getInputStream() != null) {
                getInputStream().close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        try {
            if (getOutputStream() != null) {
                getOutputStream().close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        serverDatagramConnection.closeSession(this);
    }

    @Override
    public void send(byte[] data, int offset, int len) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(data, offset, len, receivePacket.getAddress(), receivePacket.getPort());
        serverDatagramConnection.send(sendPacket);
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public InputStream getInputStream() {
        return consumeInputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return consumeOutputStream;
    }

    @Override
    public InetAddress getInetAddress() {
        return receivePacket.getAddress();
    }

}
