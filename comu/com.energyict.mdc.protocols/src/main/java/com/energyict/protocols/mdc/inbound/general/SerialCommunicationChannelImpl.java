package com.energyict.protocols.mdc.inbound.general;

import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialPort;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelInputStreamAdapter;
import com.energyict.mdc.io.ComChannelOutputStreamAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serves as an implementation of a SerialCommunicationChannel wrapped around a
 * {@link ComChannel}
 *
 * Copyrights EnergyICT
 * Date: 14/05/13
 * Time: 9:38
 */
public class SerialCommunicationChannelImpl implements SerialCommunicationChannel {

    private ComChannel comChannel;

    public SerialCommunicationChannelImpl(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    @Override
    public void setParams(int baudrate, int databits, int parity, int stopbits) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setParity(int databits, int parity, int stopbits) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setParityAndFlush(int databits, int parity, int stopbits) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setParamsAndFlush(int baudrate, int databits, int parity, int stopbits) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setBaudrate(int baudrate) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setBaudrateAndFlush(int baudrate) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getComPort() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setComPort(String strComPort) {
        //To change body of implemented methods use File | Settings | File Templates.
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
        return new ComChannelInputStreamAdapter(comChannel);
    }

    @Override
    public OutputStream getOutputStream() {
        return new ComChannelOutputStreamAdapter(comChannel);
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
}
