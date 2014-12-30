package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.InputStreamObserver;
import com.energyict.mdc.protocol.api.dialer.core.OutputStreamObserver;
import com.energyict.mdc.protocol.api.dialer.core.StreamConnection;
import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialPort;

import com.energyict.protocols.mdc.services.impl.EnvironmentPropertyService;
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
    public static final String OPEN_SERIAL_AND_FLUSH_ENVIRONMENT_PROPERTY_NAME = "openSerialAndFlush";
    public static final String SET_PARITY_AND_FLUSH_ENVIRONMENT_PROPERTY_NAME = "setParityAndFlush";
    public static final String SET_PARAMS_AND_FLUSH_ENVIRONMENT_PROPERTY_NAME = "setParamsAndFlush";
    public static final String SET_BAUDRATE_AND_FLUSH_ENVIRONMENT_PROPERTY_NAME = "setBaudrateAndFlush";
    /**
     * The name of the property that determines if the RS485 RTS is managed in software or hardware.
     * This actually only matters on Linux, where we have the RTU+Server using Exar ports that have
     * hardware driven RS485 (which is the default), but a development machine using a standard 8250 UART
     * has to do software half duplex, so in that case this property has to be set to true.
     */
    public static final String RS485_SOFTWARE_DRIVEN_ENVIRONMENT_PROPERTY_NAME = "rs485SoftwareDriven";
    public static final String IGNORE_DCD_COM_PORTS_ENVIRONMENT_PROPERTY_NAME = "ignoreDCDComPorts";
    public static final String DATAGRAM_INPUTSTREAM_BUFFER_ENVIRONMENT_PROPERTY_NAME = "datagramInputStreamBufferSize";

    /**
     * doSetParams().
     * Set the communication parameters for the open port.
     *
     * @param baudrate : 300,1200,2400,4800,9600,19200,...
     * @param databits : SerialPort.DATABITS_x (x=8,7,6,5)
     * @param parity   : SerialPort.PARITY_x (x=NONE (0),EVEN (2),ODD (1) ,MARK (3),SPACE (4))
     * @param stopbits : SerialPort.STOPBITS_x (x=1 (1),2 (2),1_5 (3))
     * @throws IOException
     */
    protected abstract void doSetParams(int baudrate, int databits, int parity, int stopbits) throws IOException;

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

    protected abstract void doOpen() throws NestedIOException;

    protected abstract void doServerOpen() throws NestedIOException;

    protected abstract void doClose() throws NestedIOException;

    protected abstract void doServerClose() throws NestedIOException;

    //****************************************************************************************
    // Serial communication parameters, default 9600,8,N,1
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

    private final EnvironmentPropertyService propertyService;

    public StreamConnectionImpl(EnvironmentPropertyService propertyService) {
        super();
        this.propertyService = propertyService;
        if ((System.getProperty("os.name").toLowerCase().contains("linux")) && (System.getProperty("os.arch").toLowerCase().contains("86"))) {
            setOsType(OS_LINUX_X86);
        } else if ((System.getProperty("os.name").toLowerCase().contains("linux")) && (System.getProperty("os.arch").toLowerCase().contains("arm"))) {
            setOsType(OS_LINUX_ARM);
        } else {
            setOsType(OS_WINDOWS);
        }
    }

    protected EnvironmentPropertyService getPropertyService() {
        return propertyService;
    }

    @Override
    public void request2Send(int nrOfBytes) {
        doRequest2Send(nrOfBytes);
    }

    @Override
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

    /**
     * setParamsAndFlush, setParams.
     * Set the communication parameters for the open port.
     *
     * @param databits : SerialPort.DATABITS_x (x=8,7,6,5)
     * @param parity   : SerialPort.PARITY_x (x=NONE,EVEN,ODD,MARK,SPACE)
     * @param stopbits : SerialPort.STOPBITS_x (x=1,2,1_5)
     * @throws IOException
     */
    @Override
    public void setParityAndFlush(int databits, int parity, int stopbits) throws IOException {
        setParity(databits, parity, stopbits);
        flushInputStream(this.propertyService.getSetParityAndFlush());
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
        flushInputStream(this.propertyService.getSetParamsAndFlush());
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
        flushInputStream(this.propertyService.getSetBaudrateAndFlush());
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
            throw new NestedIOException(e);
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
    public void open() throws NestedIOException {
        doOpen();
    }

    @Override
    public void serverOpen() throws NestedIOException {
        doServerOpen();
    }

    @Override
    public void close() throws NestedIOException {
        doClose();
    }

    @Override
    public void serverClose() throws NestedIOException {
        doServerClose();
    }

    @Override
    public void accept() throws NestedIOException {
    }

    @Override
    public void accept(final int timeOut) throws NestedIOException {
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
                throw new NestedIOException(e);
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