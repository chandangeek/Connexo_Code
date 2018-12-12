package com.energyict.dialer.coreimpl;

import java.io.IOException;
import java.io.OutputStream;

public class DatagramOutputStream extends OutputStream {

    UDPSession udpSession = null;

    public DatagramOutputStream(UDPSession udpSession) {
        this.udpSession = udpSession;
    }


    public void write(byte[] b, int off, int len) throws IOException {
        if (udpSession != null) {
            udpSession.send(b, off, len);
        }
    }


    public void write(byte[] data) throws IOException {
        if (udpSession != null) {
            udpSession.send(data, 0, data.length);
        }
    }

    public void write(int b) throws IOException {
        if (udpSession != null) {
            udpSession.send(new byte[]{(byte) b}, 0, 1);
        }
    }
}
