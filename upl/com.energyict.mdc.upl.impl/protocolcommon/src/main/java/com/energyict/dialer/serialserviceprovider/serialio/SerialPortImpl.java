package com.energyict.dialer.serialserviceprovider.serialio;

import com.energyict.dialer.uplserialserviceprovider.SerialConfig;
import com.energyict.dialer.uplserialserviceprovider.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SerialPortImpl implements SerialPort {

    private Serialio.SerialPort serialioSerialPort;
    private Serialio.SerInputStream serialioSerInputStream;
    private Serialio.SerOutputStream serialioSerOutputStream;
    private Serialio.SerialConfig serialioSerialConfig = null;

    public SerialPortImpl() {
    }

    public void init(SerialConfig serialConfig) throws IOException {
        serialioSerialPort = new Serialio.SerialPortLocal(buildSerialioSerialConfig(serialConfig));
        serialioSerInputStream = new Serialio.SerInputStream(serialioSerialPort);
        serialioSerOutputStream = new Serialio.SerOutputStream(serialioSerialPort);
    }

    public void close() throws IOException {
        serialioSerialPort.close();
    }

    private Serialio.SerialConfig buildSerialioSerialConfig(SerialConfig serialConfig) {
        if (serialioSerialConfig == null) {
            serialioSerialConfig = new Serialio.SerialConfig(serialConfig.getComPortStr());
        }
        serialioSerialConfig.setBitRate(serialConfig.getBitRate());
        serialioSerialConfig.setDataBits(serialConfig.getDataBits());
        serialioSerialConfig.setStopBits(serialConfig.getStopBits());
        serialioSerialConfig.setParity(serialConfig.getParity());
        return serialioSerialConfig;
    }

    public void configure(SerialConfig serialConfig) throws IOException {
        serialioSerialPort.configure(buildSerialioSerialConfig(serialConfig));
    }

    public InputStream getInputStream() {
        return serialioSerInputStream;
    }

    public OutputStream getOutputStream() {
        return serialioSerOutputStream;
    }

    public void setDTR(boolean dtr) throws IOException {
        serialioSerialPort.setDTR(dtr);
    }

    public void setRTS(boolean rts) throws IOException {
        serialioSerialPort.setRTS(rts);
    }

    public boolean sigCD() throws IOException {
        return serialioSerialPort.sigCD();
    }

    public boolean sigCTS() throws IOException {
        return serialioSerialPort.sigCTS();
    }

    public boolean sigDSR() throws IOException {
        return serialioSerialPort.sigDSR();
    }

    public boolean sigRing() throws IOException {
        return serialioSerialPort.sigRing();
    }

    public int txBufCount() throws IOException {
        return serialioSerialPort.txBufCount();
    }

    public void setRcvTimeout(int receiveTimeout) {
        serialioSerInputStream.setRcvTimeout(receiveTimeout);
    }

    public void setWriteDrain(boolean writeDrain) {
        serialioSerOutputStream.setWriteDrain(writeDrain);

    }


}
