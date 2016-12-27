package com.energyict.protocolimpl.utils;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.core.StreamConnection;
import com.energyict.dialer.serialserviceprovider.SerialPort;
import com.energyict.protocol.tools.InputStreamObserver;
import com.energyict.protocol.tools.OutputStreamObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Copyrights EnergyICT
 * Date: 21-apr-2010
 * Time: 13:36:23
 */
public class VirtualSerialCommunicationChannel implements StreamConnection {

    private VirtualDeviceDialer virtualDeviceDialer;

    public VirtualSerialCommunicationChannel(VirtualDeviceDialer virtualDeviceDialer) {
        this.virtualDeviceDialer = virtualDeviceDialer;
    }

    public void setParams(int baudrate, int databits, int parity, int stopbits) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setParity(int databits, int parity, int stopbits) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setParityAndFlush(int databits, int parity, int stopbits) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setParamsAndFlush(int baudrate, int databits, int parity, int stopbits) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setBaudrate(int baudrate) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setBaudrateAndFlush(int baudrate) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getComPort() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setComPort(String strComPort) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean sigRing() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean sigDSR() throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SerialPort getSerialPort() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InputStream getInputStream() {
        return virtualDeviceDialer.getInputStream();
    }

    public OutputStream getOutputStream() {
        return virtualDeviceDialer.getOutputStream();
    }

    public void setStreams(InputStream is, OutputStream os) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void flushInputStream(long delay) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStreamObservers(InputStreamObserver iso, OutputStreamObserver oso) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void open() throws NestedIOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void serverOpen() throws NestedIOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void accept() throws NestedIOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void accept(int i) throws NestedIOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void close() throws NestedIOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void serverClose() throws NestedIOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isOpen() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void write(String strData, int iTimeout) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void write(String strData) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Socket getSocket() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void request2Send(int nrOfBytes) {}
    public void request2Receive(int nrOfBytes) {}
    public void request2SendV25(int nrOfBytes) {}
    public void request2ReceiveV25(int nrOfBytes) {}
    public void request2SendRS485() {}
    public void request2ReceiveRS485(int nrOfBytes) {}
    public void setDelay(long halfDuplexTXDelay) {}
    public boolean sigCD() throws IOException {return false;}
    public boolean sigCTS() throws IOException {return false;}
    public void setDTR(boolean dtr) throws IOException {}
    public void setRTS(boolean rts) throws IOException {}
}
