package com.energyict.mdc.protocol.api.dialer.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-02 (12:04)
 */
public interface UDPSession {

    public void receive(DatagramPacket receivePacket) throws IOException;

    public void close();

    public void send(byte[] data, int offset, int len) throws IOException;

    public String getSignature();

    public InputStream getInputStream();

    public OutputStream getOutputStream();

    public InetAddress getInetAddress();

}