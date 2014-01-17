/*
 * StreamConnectionImpl.java
 *
 * Created on 13 april 2004, 10:37
 */

package com.energyict.dialer.core.impl;


import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.InputStreamObserver;
import com.energyict.mdc.protocol.api.dialer.core.OutputStreamObserver;
import com.energyict.mdc.protocol.api.dialer.core.StreamConnection;
import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialPort;
import com.energyict.protocols.util.MonitoredInputStream;
import com.energyict.protocols.util.MonitoredOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koen
 */
public abstract class StreamConnectionImpl implements StreamConnection {

    private static final Logger LOGGER = Logger.getLogger(StreamConnectionImpl.class.getName());

    //****************************************************************************************
    // delegate SerialCommunicationChannel interface methods
    //****************************************************************************************

    protected abstract void doSetParams(int iBaudrate, int iDatabits, int iParity, int iStopbits) throws java.io.IOException;

    protected abstract void doSetComPort(String strComPort);

    protected abstract boolean doSigCD() throws IOException;

    protected abstract boolean doSigCTS() throws IOException;

    protected abstract void doSetDTR(boolean dtr) throws IOException;

    protected abstract void doSetRTS(boolean rts) throws IOException;

    protected abstract boolean doSigDSR() throws IOException;

    protected abstract boolean doSigRing() throws IOException;

    protected abstract SerialPort doGetSerialPort();

    //****************************************************************************************
    // delegate HalfDuplexController interface methods
    //****************************************************************************************

    protected abstract void doRequest2Send(int nrOfBytes);

    protected abstract void doRequest2Receive(int nrOfBytes);

    protected abstract void doRequest2SendV25(int nrOfBytes);

    protected abstract void doRequest2ReceiveV25(int nrOfBytes);

    protected abstract void doRequest2SendRS485();

    protected abstract void doRequest2ReceiveRS485(int nrOfBytes);

    //****************************************************************************************
    // delegate StreamConnection interface methods
    //****************************************************************************************

    protected abstract void doOpen() throws NestedIOException;

    protected abstract void doServerOpen() throws NestedIOException;

    protected abstract void doClose() throws NestedIOException;

    protected abstract void doServerClose() throws NestedIOException;

    //****************************************************************************************
    // Serial communication parameters, default 9600,8,N,1
    // Used with SerialPortStreamConnection via Serial service provider objects
    // Used with SocketStreamConnection via Escape sequences to set parameters remote
    String strComPort = null;
    int baudrate = 9600;
    int databits = 8;
    int parity = 0;
    int stopbits = 1;

    //****************************************************************************************
    // Common core properties
    boolean boolOpen = false;

    final int OS_WINDOWS = 0;
    final int OS_LINUX_X86 = 1;
    final int OS_LINUX_ARM = 2;
    private int osType;
    private boolean initDone = false;

    //****************************************************************************************
    // Exposed common core properties
    InputStream inputStream = null;
    OutputStream outputStream = null;
    InputStreamObserver inputStreamObserver = null;
    OutputStreamObserver outputStreamObserver = null;

    /**
     * Creates a new instance of StreamConnectionImpl
     */
    public StreamConnectionImpl() {
        if ((System.getProperty("os.name").toLowerCase().contains("linux")) && (System.getProperty("os.arch").toLowerCase().contains("86"))) {
            setOsType(OS_LINUX_X86);
        } else if ((System.getProperty("os.name").toLowerCase().contains("linux")) && (System.getProperty("os.arch").toLowerCase().contains("arm"))) {
            setOsType(OS_LINUX_ARM);
        } else {
            setOsType(OS_WINDOWS);
        }
    }


    //****************************************************************************************
    // Implementation of interface HalfDuplexController
    //****************************************************************************************

    public void request2Send(int nrOfBytes) {
        doRequest2Send(nrOfBytes);
    }

    public void request2SendV25(int nrOfBytes) {
        doRequest2SendV25(nrOfBytes);
    }

    // one time extra initialization at first send!

    private void init() {

        if (!initDone) {
            if (isOsTypeLINUX_X86()) {
                if ((halfDuplexTXDelay < 0) && (getComPort().contains("/dev/ttyXR"))) {
                    try {
                        if (halfDuplexTXDelay == -1) {
                            ((DeviceControl) Class.forName("com.energyict.concentrator.jniexar.ExarControlJNI").getMethod("getInstance").invoke(null)).rs485Mode(getComPort(), 8);
                        } else {
                            ((DeviceControl) Class.forName("com.energyict.concentrator.jniexar.ExarControlJNI").getMethod("getInstance").invoke(null)).rs485Mode(getComPort(), (int) Math.abs(halfDuplexTXDelay));
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            } else if (isOsTypeLINUX_ARM()) {
                if ((halfDuplexTXDelay < 0) && (getComPort().contains("/dev/ttyS"))) {
                    try {
                        if (halfDuplexTXDelay == -1) {
                            ((DeviceControl) Class.forName("com.energyict.concentrator.jniexar.AtmelUartControl").getMethod("getInstance").invoke(null)).rs485Mode(getComPort(), 8);
                        } else {
                            ((DeviceControl) Class.forName("com.energyict.concentrator.jniexar.AtmelUartControl").getMethod("getInstance").invoke(null)).rs485Mode(getComPort(), (int) Math.abs(halfDuplexTXDelay));
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
        initDone = true;
    } // private void init()

    public void request2SendRS485() {
        init();
        doRequest2SendRS485();
    }

    long halfDuplexTXDelay = 0;

    public void setDelay(long halfDuplexTXDelay) {
        this.halfDuplexTXDelay = halfDuplexTXDelay;
    }

    public void request2Receive(int nrOfBytes) {
        doRequest2Receive(nrOfBytes);
    }

    public void request2ReceiveV25(int nrOfBytes) {
        doRequest2ReceiveV25(nrOfBytes);
    }

    public void request2ReceiveRS485(int nrOfBytes) {
        init();
        doRequest2ReceiveRS485(nrOfBytes);
    }

    //****************************************************************************************
    // Implementation of interface SerialCommunicationChannel
    //****************************************************************************************


    final int SET_PARITY_AND_FLUSH = getIntProperty("setParityAndFlush", 500);
    final int SET_PARAMS_AND_FLUSH = getIntProperty("setParamsAndFlush", 500);
    final int SET_BAUDRATE_AND_FLUSH = getIntProperty("setBaudrateAndFlush", 500);

    /**
     * setParamsAndFlush, setParams.
     * Set the communication parameters for the open port.
     *
     * @param databits : SerialPort.DATABITS_x (x=8,7,6,5)
     * @param parity   : SerialPort.PARITY_x (x=NONE,EVEN,ODD,MARK,SPACE)
     * @param stopbits : SerialPort.STOPBITS_x (x=1,2,1_5)
     * @throws IOException
     */
    public void setParityAndFlush(int databits, int parity, int stopbits) throws IOException {
        setParity(databits, parity, stopbits);
        flushInputStream(SET_PARITY_AND_FLUSH);
    }

    public void setParity(int databits, int parity, int stopbits) throws IOException {
        this.databits = databits;
        this.parity = parity;
        this.stopbits = stopbits;
        doSetParams(baudrate, databits, parity, stopbits);
    }

    public void setParamsAndFlush(int baudrate, int databits, int parity, int stopbits) throws IOException {
        setParams(baudrate, databits, parity, stopbits);
        flushInputStream(SET_PARAMS_AND_FLUSH);
    }

    public void setParams(int baudrate, int databits, int parity, int stopbits) throws IOException {
        this.baudrate = baudrate;
        this.databits = databits;
        this.parity = parity;
        this.stopbits = stopbits;
        doSetParams(baudrate, databits, parity, stopbits);
    }

    public void setBaudrateAndFlush(int baudrate) throws IOException {
        setBaudrate(baudrate);
        flushInputStream(SET_BAUDRATE_AND_FLUSH);
    }

    public void setBaudrate(int baudrate) throws IOException {
        this.baudrate = baudrate;
        doSetParams(baudrate, databits, parity, stopbits);
    }

    public String getComPort() {
        return strComPort;
    }

    public void setComPort(String strComPort) {
        this.strComPort = strComPort;
        doSetComPort(strComPort);
    }

    public boolean sigDSR() throws IOException {
        return doSigDSR();
    }

    public boolean sigRing() throws IOException {
        return doSigRing();
    }

    public boolean sigCD() throws IOException {
        return doSigCD();
    }

    public boolean sigCTS() throws IOException {
        return doSigCTS();
    }

    public void setDTR(boolean dtr) throws IOException {
        doSetDTR(dtr);
    }

    public void setRTS(boolean rts) throws IOException {
        doSetRTS(rts);
    }

    //****************************************************************************************
    // Implementation of interface StreamConnection
    //****************************************************************************************

    public void flushInputStream(long delay) throws IOException {
        try {
            Thread.sleep(delay);
            flushInputStream();
        } catch (InterruptedException e) {
            throw new NestedIOException(e);
        }
    } // public void flushInputStream(long delay)

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setStreams(InputStream is, OutputStream os) {
        if ((inputStreamObserver != null) && (outputStreamObserver != null)) {
            MonitoredInputStream monitoredInputStream = new MonitoredInputStream(is, inputStreamObserver);
            MonitoredOutputStream monitoredOutputStream = new MonitoredOutputStream(os, outputStreamObserver);
            inputStream = monitoredInputStream;
            outputStream = monitoredOutputStream;
        } else {
            outputStream = os;
            inputStream = is;
        }
    } // protected void initStreams(InputStream is,OutputStream os)

    public void setStreamObservers(InputStreamObserver iso, OutputStreamObserver oso) {
        inputStreamObserver = iso;
        outputStreamObserver = oso;

        if ((inputStream != null) && (outputStream != null)) {
            MonitoredInputStream monitoredInputStream = new MonitoredInputStream(inputStream, inputStreamObserver);
            MonitoredOutputStream monitoredOutputStream = new MonitoredOutputStream(outputStream, outputStreamObserver);
            inputStream = monitoredInputStream;
            outputStream = monitoredOutputStream;
        }

    }

    public boolean isOpen() {
        return boolOpen;
    }

    public void open() throws NestedIOException {
        doOpen();
    }

    public void serverOpen() throws NestedIOException {
        doServerOpen();
    }

    public void close() throws NestedIOException {
        doClose();
    }

    public void serverClose() throws NestedIOException {
        doServerClose();
    }

    public void accept() throws NestedIOException {
    }

    public void accept(final int timeOut) throws NestedIOException {
    }

    //****************************************************************************************
    // Private methods
    //****************************************************************************************

    private void flushInputStream() throws IOException {
        while (inputStream.available() != 0) {
            inputStream.read();
        }
    }

    //****************************************************************************************
    // Protected methods
    //****************************************************************************************

    public SerialPort getSerialPort() {
        return doGetSerialPort();
    }

    public void write(String strData, int iTimeout) throws IOException {
        if (isOpen()) {
            try {
                // Some devices need a delay before sending new commands...
                Thread.sleep(iTimeout);
                outputStream.write(strData.getBytes());
            } catch (InterruptedException e) {
                throw new NestedIOException(e);
            }
        } else {
            throw new IOException("StreamConnectionImpl, Write, Port not open");
        }

    } // public void write(String strData, int iTimeout)

    public void write(String strData) throws IOException {
        if (isOpen()) {
            outputStream.write(strData.getBytes());
        } else {
            throw new IOException("Writeerror : Port not open");
        }

    } // protected void write(String strData)

    protected int getIntProperty(String key, int defaultValue) {
        String value = Environment.DEFAULT.get().getProperty(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            // silently ignore
            return defaultValue;
        }
    }

    public int getOsType() {
        return osType;
    }

    public boolean isOsTypeLINUX() {
        return (osType == OS_LINUX_X86) || (osType == OS_LINUX_ARM);
    }

    private boolean isOsTypeLINUX_X86() {
        return osType == OS_LINUX_X86;
    }

    private boolean isOsTypeLINUX_ARM() {
        return osType == OS_LINUX_ARM;
    }

    public boolean isOsTypeWINDOWS() {
        return osType == OS_WINDOWS;
    }

    private void setOsType(int osType) {
        this.osType = osType;
    }
}
