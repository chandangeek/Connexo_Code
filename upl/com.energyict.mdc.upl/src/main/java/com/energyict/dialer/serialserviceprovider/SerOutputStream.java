package com.energyict.dialer.serialserviceprovider;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author kvds
 *         Decorator over the serialport service provided outputstream
 */
public class SerOutputStream extends OutputStream {

    private final SerialPort serialPort;

    public SerOutputStream(SerialPort serialPort) {
        super();
        this.serialPort = serialPort;
    }

    @Override
    public void close() throws IOException {
        serialPort.getOutputStream().close();
    }

    @Override
    public void flush() throws IOException {
        serialPort.getOutputStream().flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        serialPort.getOutputStream().write(b);
    }

    @Override
    public void write(int b) throws IOException {
        serialPort.getOutputStream().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        serialPort.getOutputStream().write(b, off, len);
    }

    public void setWriteDrain(boolean writeDrain) {
        serialPort.setWriteDrain(writeDrain);
    }

}