package com.energyict.dialer.coreimpl;

import com.energyict.mdc.upl.RuntimeEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class UDPSession { //implements Runnable {

    private static final Log logger = LogFactory.getLog(UDPSession.class);

    DatagramPacket receivePacket;
    ServerDatagramConnection serverDatagramConnection;
    String signature;

    DatagramInputStream consumeInputStream = null;
    DatagramOutputStream consumeOutputStream = null;

    public UDPSession(ServerDatagramConnection serverDatagramConnection, RuntimeEnvironment runtimeEnvironment, String signature) throws IOException {
        this.serverDatagramConnection = serverDatagramConnection;
        this.signature = signature;
        consumeInputStream = new DatagramInputStream(runtimeEnvironment, new PipedOutputStream());
        consumeOutputStream = new DatagramOutputStream(this);
    }


    public void receive(DatagramPacket receivePacket) throws IOException {
        this.receivePacket = receivePacket;
        if (logger.isDebugEnabled()) {
            logger.debug("UDPSession " + signature + " received " + receivePacket.getLength() + " bytes of data...");
        }
        consumeInputStream.write(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
    }

    public void close() {
        try {
            if (getInputStream() != null) {
                getInputStream().close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if (getOutputStream() != null) {
                getOutputStream().close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        serverDatagramConnection.closeSession(this);
    }

    public void send(byte[] data, int offset, int len) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(data, offset, len, receivePacket.getAddress(), receivePacket.getPort());
        serverDatagramConnection.send(sendPacket);
    }

    public String getSignature() {
        return signature;
    }

    public InputStream getInputStream() {
        return consumeInputStream;
    }

    public OutputStream getOutputStream() {
        return consumeOutputStream;
    }

    public InetAddress getInetAddress() {
        return receivePacket.getAddress();
    }

}
