/*
 * StreamConnectionImpl.java
 *
 * Created on 13 april 2004, 10:37
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.upl.RuntimeEnvironment;

import com.energyict.dialer.core.StreamConnection;
import com.energyict.dialer.serialserviceprovider.SerialPort;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.tools.InputStreamObserver;
import com.energyict.protocol.tools.MonitoredInputStream;
import com.energyict.protocol.tools.MonitoredOutputStream;
import com.energyict.protocol.tools.OutputStreamObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * @author Koen
 */
public abstract class StreamConnectionImpl implements StreamConnection {

    private static final Log logger = LogFactory.getLog(StreamConnectionImpl.class);

    protected abstract void doSetParams(int iBaudrate, int iDatabits, int iParity, int iStopbits) throws java.io.IOException;

    protected abstract void doSetComPort(String strComPort);

    protected abstract boolean doSigCD() throws IOException;

    protected abstract boolean doSigCTS() throws IOException;

    protected abstract void doSetDTR(boolean dtr) throws IOException;

    protected abstract void doSetRTS(boolean rts) throws IOException;

    protected abstract boolean doSigDSR() throws IOException;

    protected abstract boolean doSigRing() throws IOException;

    protected abstract SerialPort doGetSerialPort();

    protected abstract void doRequest2Send(int nrOfBytes);

    protected abstract void doRequest2Receive(int nrOfBytes);

    protected abstract void doRequest2SendV25(int nrOfBytes);

    protected abstract void doRequest2ReceiveV25(int nrOfBytes);

    protected abstract void doRequest2SendRS485();

    protected abstract void doRequest2ReceiveRS485(int nrOfBytes);

    protected abstract void doOpen() throws IOException;

    protected abstract void doServerOpen() throws IOException;

    protected abstract void doClose() throws IOException;

    protected abstract void doServerClose() throws IOException;

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
    private final RuntimeEnvironment runtimeEnvironment;

    public StreamConnectionImpl(RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
        if ((System.getProperty("os.name").toLowerCase().contains("linux")) && (System.getProperty("os.arch").toLowerCase().contains("86"))) {
            setOsType(OS_LINUX_X86);
        } else if ((System.getProperty("os.name").toLowerCase().contains("linux")) && (System.getProperty("os.arch").toLowerCase().contains("arm"))) {
            setOsType(OS_LINUX_ARM);
        } else {
            setOsType(OS_WINDOWS);
        }
    }

    protected RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    @Override
    public void request2Send(int nrOfBytes) {
        doRequest2Send(nrOfBytes);
    }

    @Override
    public void request2SendV25(int nrOfBytes) {
        doRequest2SendV25(nrOfBytes);
    }

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
                        logger.error(e.getMessage(), e);
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
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        initDone = true;
    }

    @Override
    public void request2SendRS485() {
        init();
        doRequest2SendRS485();
    }

    long halfDuplexTXDelay = 0;

    @Override
    public void setDelay(long halfDuplexTXDelay) {
        this.halfDuplexTXDelay = halfDuplexTXDelay;
    }

    @Override
    public void request2Receive(int nrOfBytes) {
        doRequest2Receive(nrOfBytes);
    }

    @Override
    public void request2ReceiveV25(int nrOfBytes) {
        doRequest2ReceiveV25(nrOfBytes);
    }

    @Override
    public void request2ReceiveRS485(int nrOfBytes) {
        init();
        doRequest2ReceiveRS485(nrOfBytes);
    }

    final int SET_PARITY_AND_FLUSH = getIntProperty("setParityAndFlush", 500);
    final int SET_PARAMS_AND_FLUSH = getIntProperty("setParamsAndFlush", 500);
    final int SET_BAUDRATE_AND_FLUSH = getIntProperty("setBaudrateAndFlush", 500);

    public void setParityAndFlush(int databits, int parity, int stopbits) throws IOException {
        setParity(databits, parity, stopbits);
        flushInputStream(SET_PARITY_AND_FLUSH);
    }

    @Override
    public void setParity(int databits, int parity, int stopbits) throws IOException {
        this.databits = databits;
        this.parity = parity;
        this.stopbits = stopbits;
        doSetParams(baudrate, databits, parity, stopbits);
    }

    @Override
    public void setParamsAndFlush(int baudrate, int databits, int parity, int stopbits) throws IOException {
        setParams(baudrate, databits, parity, stopbits);
        flushInputStream(SET_PARAMS_AND_FLUSH);
    }

    @Override
    public void setParams(int baudrate, int databits, int parity, int stopbits) throws IOException {
        this.baudrate = baudrate;
        this.databits = databits;
        this.parity = parity;
        this.stopbits = stopbits;
        doSetParams(baudrate, databits, parity, stopbits);
    }

    @Override
    public void setBaudrateAndFlush(int baudrate) throws IOException {
        setBaudrate(baudrate);
        flushInputStream(SET_BAUDRATE_AND_FLUSH);
    }

    @Override
    public void setBaudrate(int baudrate) throws IOException {
        this.baudrate = baudrate;
        doSetParams(baudrate, databits, parity, stopbits);
    }

    @Override
    public String getComPort() {
        return strComPort;
    }

    @Override
    public void setComPort(String strComPort) {
        this.strComPort = strComPort;
        doSetComPort(strComPort);
    }

    @Override
    public boolean sigDSR() throws IOException {
        return doSigDSR();
    }

    @Override
    public boolean sigRing() throws IOException {
        return doSigRing();
    }

    @Override
    public boolean sigCD() throws IOException {
        return doSigCD();
    }

    @Override
    public boolean sigCTS() throws IOException {
        return doSigCTS();
    }

    @Override
    public void setDTR(boolean dtr) throws IOException {
        doSetDTR(dtr);
    }

    @Override
    public void setRTS(boolean rts) throws IOException {
        doSetRTS(rts);
    }

    @Override
    public void flushInputStream(long delay) throws IOException {
        try {
            Thread.sleep(delay);
            flushInputStream();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
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
    }

    @Override
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

    @Override
    public boolean isOpen() {
        return boolOpen;
    }

    @Override
    public void open() throws IOException {
        doOpen();
    }

    @Override
    public void serverOpen() throws IOException {
        doServerOpen();
    }

    @Override
    public void close() throws IOException {
        doClose();
    }

    @Override
    public void serverClose() throws IOException {
        doServerClose();
    }

    @Override
    public void accept() throws IOException {
    }

    @Override
    public void accept(final int timeOut) throws IOException {
    }

    private void flushInputStream() throws IOException {
        while (inputStream.available() != 0) {
            inputStream.read();
        }
    }

    @Override
    public SerialPort getSerialPort() {
        return doGetSerialPort();
    }

    @Override
    public void write(String strData, int iTimeout) throws IOException {
        if (isOpen()) {
            try {
                // Some devices need a delay before sending new commands...
                Thread.sleep(iTimeout);
                outputStream.write(strData.getBytes());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
        } else {
            throw new IOException("StreamConnectionImpl, Write, Port not open");
        }

    }

    @Override
    public void write(String strData) throws IOException {
        if (isOpen()) {
            outputStream.write(strData.getBytes());
        } else {
            throw new IOException("Writeerror : Port not open");
        }

    }

    protected int getIntProperty(String key, int defaultValue) {
        Optional<String> propertyValue = this.runtimeEnvironment.getProperty(key);
        if (propertyValue.isPresent()) {
            try {
                return Integer.parseInt(propertyValue.get());
            } catch (NumberFormatException ex) {
                // silently ignore
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    protected boolean getBooleanProperty(String key, boolean defaultValue) {
        Optional<String> propertyValue = this.runtimeEnvironment.getProperty(key);
        if (propertyValue.isPresent()) {
            return Boolean.valueOf(propertyValue.get());
        } else {
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
