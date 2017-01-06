package com.energyict.dialer.serialserviceprovider;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author kvds
 *         Decorator over the serialport service provided inputstream
 */
public class SerInputStream extends InputStream {

    private final SerialPort serialPort;

    public SerInputStream(SerialPort serialPort) {
        super();
        this.serialPort = serialPort;
    }

    @Override
    public int available() throws IOException {
        return serialPort.getInputStream().available();
    }

    @Override
    public void close() throws IOException {
        serialPort.getInputStream().close();
    }

    @Override
    public void mark(int readlimit) {
        serialPort.getInputStream().mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return serialPort.getInputStream().markSupported();
    }

    @Override
    public int read() throws IOException {
        return serialPort.getInputStream().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return serialPort.getInputStream().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return serialPort.getInputStream().read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        serialPort.getInputStream().reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return serialPort.getInputStream().skip(n);
    }

    /**
     * @param receiveTimeout
     * @throws IOException
     */
    public void setRcvTimeout(int receiveTimeout) throws IOException {
        serialPort.setRcvTimeout(receiveTimeout);
    }
}