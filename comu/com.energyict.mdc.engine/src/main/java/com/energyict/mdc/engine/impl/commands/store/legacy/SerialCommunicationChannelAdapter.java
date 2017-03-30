/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.uplserialserviceprovider.SerialPort;
import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.channel.serial.Parities;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.io.ComChannelInputStreamAdapter;
import com.energyict.mdc.io.ComChannelOutputStreamAdapter;
import com.energyict.mdc.protocol.SerialPortComChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

public class SerialCommunicationChannelAdapter implements SerialCommunicationChannel {

    private final ServerSerialPort serialPort;

    private ComChannelInputStreamAdapter inputStream;
    private ComChannelOutputStreamAdapter outputStream;

    public SerialCommunicationChannelAdapter(final SerialPortComChannel serialPortComChannel) {
        this.serialPort = serialPortComChannel.getSerialPort();
        this.inputStream = new ComChannelInputStreamAdapter(serialPortComChannel);
        this.outputStream =  new ComChannelOutputStreamAdapter(serialPortComChannel);
    }

    protected String parityToNewFormat(int parity) {
        switch (parity) {
            case SerialCommunicationChannel.PARITY_NONE:
                return Parities.NONE.getParity();
            case SerialCommunicationChannel.PARITY_EVEN:
                return Parities.EVEN.getParity();
            case SerialCommunicationChannel.PARITY_MARK:
                return Parities.MARK.getParity();
            case SerialCommunicationChannel.PARITY_ODD:
                return Parities.ODD.getParity();
            case SerialCommunicationChannel.PARITY_SPACE:
                return Parities.SPACE.getParity();
            default:
                return Parities.NONE.getParity();
        }
    }

    protected BigDecimal stopBitsToNewFormat(int stopbits) {
        float newStopBits = 1;
        switch (stopbits) {
            case SerialCommunicationChannel.STOPBITS_1:
                newStopBits = 1;
                break;
            case SerialCommunicationChannel.STOPBITS_1_5:
                newStopBits = 1.5f;
                break;
            case SerialCommunicationChannel.STOPBITS_2:
                newStopBits = 2;
                break;
        }
        return new BigDecimal(newStopBits);
    }

    @Override
    public void setParams(int baudrate, int databits, int parity, int stopbits) throws IOException {
        SerialPortConfiguration serialPortConfiguration = this.serialPort.getSerialPortConfiguration();
        serialPortConfiguration.setBaudrate(BaudrateValue.valueFor(new BigDecimal(baudrate)));
        serialPortConfiguration.setNrOfDataBits(NrOfDataBits.valueFor(new BigDecimal(databits)));
        serialPortConfiguration.setNrOfStopBits(NrOfStopBits.valueFor(stopBitsToNewFormat(stopbits)));
        serialPortConfiguration.setParity(Parities.valueFor(parityToNewFormat(parity)));
        this.serialPort.updatePortConfiguration(serialPortConfiguration);
    }

    @Override
    public void setParity(int databits, int parity, int stopbits) throws IOException {
        SerialPortConfiguration serialPortConfiguration = this.serialPort.getSerialPortConfiguration();
        serialPortConfiguration.setNrOfDataBits(NrOfDataBits.valueFor(new BigDecimal(databits)));
        serialPortConfiguration.setNrOfStopBits(NrOfStopBits.valueFor(stopBitsToNewFormat(stopbits)));
        serialPortConfiguration.setParity(Parities.valueFor(parityToNewFormat(parity)));
        this.serialPort.updatePortConfiguration(serialPortConfiguration);
    }

    @Override
    public void setParityAndFlush(int databits, int parity, int stopbits) throws IOException {
        setParity(databits, parity, stopbits);
        flushInputStream();
    }

    @Override
    public void setParamsAndFlush(int baudrate, int databits, int parity, int stopbits) throws IOException {
        setParams(baudrate, databits, parity, stopbits);
        flushInputStream();
    }

    @Override
    public void setBaudrate(int baudrate) throws IOException {
        SerialPortConfiguration serialPortConfiguration = this.serialPort.getSerialPortConfiguration();
        serialPortConfiguration.setBaudrate(BaudrateValue.valueFor(new BigDecimal(baudrate)));
        this.serialPort.updatePortConfiguration(serialPortConfiguration);
    }

    @Override
    public void setBaudrateAndFlush(int baudrate) throws IOException {
        setBaudrate(baudrate);
        flushInputStream();
    }

    @Override
    public String getComPort() {
        return this.serialPort.getSerialPortConfiguration().getComPortName();
    }

    @Override
    public void setComPort(String strComPort) {
        // can not set comport
    }

    @Override
    public boolean sigRing() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean sigDSR() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SerialPort getSerialPort() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public void request2Send(int nrOfBytes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void request2Receive(int nrOfBytes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void request2SendV25(int nrOfBytes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void request2ReceiveV25(int nrOfBytes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void request2SendRS485() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void request2ReceiveRS485(int nrOfBytes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDelay(long halfDuplexTXDelay) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean sigCD() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean sigCTS() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDTR(boolean dtr) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRTS(boolean rts) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void flushInputStream() throws IOException {
        while (inputStream.available() != 0) {
            inputStream.read();
        }
    }
}
