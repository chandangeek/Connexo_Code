package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.UDPSession;
import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialConfig;
import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialPort;

import com.energyict.protocols.mdc.dialer.serialserviceprovider.SerInputStream;
import com.energyict.protocols.mdc.dialer.serialserviceprovider.SerOutputStream;
import com.energyict.protocols.mdc.dialer.serialserviceprovider.SerialPortServiceProvider;
import com.energyict.protocols.mdc.services.impl.EnvironmentPropertyService;

import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koen
 */
public class SerialPortStreamConnection extends StreamConnectionImpl {

    private static final Logger LOGGER = Logger.getLogger(SerialPortStreamConnection.class.getName());

    // Serial service provider objects for serial port communication
    private SerialConfig serialConfig = null;
    private SerialPort serialPort = null;

    private final boolean rs485HardwareDriven;

    public SerialPortStreamConnection(String strComPort, EnvironmentPropertyService propertyService) {
        super(propertyService);
        setComPort(strComPort);
        boolOpen = false;
        this.rs485HardwareDriven = !propertyService.isRs485SoftwareDriven();
    }

    @Override
    protected void doSetParams(int baudrate, int databits, int parity, int stopbits) throws IOException {
        int baudIndex;
        switch (baudrate) {
            case 300:
                baudIndex = 2;
                break;
            case 600:
                baudIndex = 3;
                break;
            case 1200:
                baudIndex = 4;
                break;
            case 2400:
                baudIndex = 5;
                break;
            case 4800:
                baudIndex = 6;
                break;
            case 9600:
                baudIndex = 7;
                break;
            case 19200:
                baudIndex = 8;
                break;
            case 38400:
                baudIndex = 9;
                break;
            case 57600:
                baudIndex = 10;
                break;
            case 115200:
                baudIndex = 11;
                break;
            default:
                throw new IOException("SerialPortConnection, doSetParams, invalid baudrate " + baudrate);
        }

        serialConfig.setBitRate(baudIndex); //SerialConfig.BR_19200);
        serialConfig.setDataBits(databits - 5); //SerialConfig.LN_8BITS);
        serialConfig.setStopBits(stopbits - 1); //SerialConfig.LN_1BITS);
        serialConfig.setParity(parity); //SerialConfig.PY_NONE);

        if (serialPort != null) {
            serialPort.configure(serialConfig);
        }
    }

    @Override
    protected void doSetComPort(String strComPort) {
        serialConfig = new SerialConfig(strComPort);
    }

    private boolean isIgnoreDCD() {
        String ignoreDCDComPorts = this.getPropertyService().getDcdComPortsToIgnore();
        StringTokenizer strTok = new StringTokenizer(ignoreDCDComPorts, ",");
        while (strTok.hasMoreTokens()) {
            if (getComPort().equals(strTok.nextToken())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean doSigCD() throws IOException {
        return !isIgnoreDCD() && serialPort.sigCD();
    }

    @Override
    protected boolean doSigCTS() throws IOException {
        return serialPort.sigCTS();
    }

    @Override
    protected boolean doSigDSR() throws IOException {
        return serialPort.sigDSR();
    }

    @Override
    protected boolean doSigRing() throws IOException {
        return serialPort.sigRing();
    }

    @Override
    protected void doSetDTR(boolean dtr) throws IOException {
        serialPort.setDTR(dtr);
    }

    @Override
    protected void doSetRTS(boolean rts) throws IOException {
        serialPort.setRTS(rts);
    }


    @Override
    protected void doRequest2Send(int nrOfBytes) {
        try {
            serialPort.setRTS(true);
            long returnTime = System.currentTimeMillis() + 2000;
            while (true) {
                Thread.sleep(50);
                if (serialPort.sigCTS()) {
                    return;
                }
                if (System.currentTimeMillis() - returnTime > 0) {
                    return;
                }
            }
        } catch (InterruptedException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doRequest2Receive(int nrOfBytes) {
        try {
            // wait for tx buffer empty...
            long returnTime = System.currentTimeMillis() + 10000;
            while (serialPort.txBufCount() > 0) {
                Thread.sleep(100);
                if (System.currentTimeMillis() - returnTime > 0) {
                    return;
                }
            }
            // delay another delay ms...
            long delay = (((nrOfBytes * 10) * 1000) / baudrate) + halfDuplexTXDelay;
            Thread.sleep(delay);

            // drop rts
            serialPort.setRTS(false);

            // wait for cts dropped
            returnTime = System.currentTimeMillis() + 2000;
            while (true) {
                Thread.sleep(50);
                if (!serialPort.sigCTS()) {
                    return;
                }
                if (System.currentTimeMillis() - returnTime > 0) {
                    return;
                }
            }
        } catch (InterruptedException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doRequest2SendV25(int nrOfBytes) {
        try {
            // wait for CD false
            long returnTime = System.currentTimeMillis() + 2000;
            while (true) {
                Thread.sleep(10);
                if (!serialPort.sigCD()) {
                    break;
                }
                if (System.currentTimeMillis() - returnTime > 0) {
                    break;
                }
            }

            // raise RTS
            serialPort.setRTS(true);

            // wait for CTS true
            returnTime = System.currentTimeMillis() + 2000;
            while (true) {
                Thread.sleep(50);
                if (serialPort.sigCTS()) {
                    return;
                }
                if (System.currentTimeMillis() - returnTime > 0) {
                    return;
                }
            }
        } catch (InterruptedException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doRequest2ReceiveV25(int nrOfBytes) {
        try {
            // wait for tx buffer empty...
            long returnTime = System.currentTimeMillis() + 10000;
            while (serialPort.txBufCount() > 0) {
                Thread.sleep(100);
                if (System.currentTimeMillis() - returnTime > 0) {
                    return;
                }
            }
            // delay another delay ms...
            long delay = (((nrOfBytes * 10) * 1000) / baudrate) + halfDuplexTXDelay;
            Thread.sleep(delay);

            // drop rts
            serialPort.setRTS(false);

            // wait for CD true
            returnTime = System.currentTimeMillis() + 2000;
            while (true) {
                Thread.sleep(10);
                if (serialPort.sigCD()) {
                    break;
                }
                if (System.currentTimeMillis() - returnTime > 0) {
                    break;
                }
            }
        } catch (InterruptedException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doRequest2SendRS485() {
        try {
            // if halfDuplexTXDelay>0, use software RST control
            if ((halfDuplexTXDelay > 0) || ((isOsTypeWINDOWS()) && (halfDuplexTXDelay != 0))) {
                if (isOsTypeLINUX() && this.rs485HardwareDriven) {
                    serialPort.setRTS(false);
                    try {
                        long delay = System.currentTimeMillis() + 1;
                        while (true) {
                            Thread.sleep(0);
                            if (delay < System.currentTimeMillis()) {
                                break;
                            }
                        }

                    } catch (InterruptedException e) {

                    }
                } else {
                    serialPort.setRTS(true);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doRequest2ReceiveRS485(int nrOfBytes) {
        try {
            // if halfDuplexTXDelay>0, use software RST control
            if ((halfDuplexTXDelay > 0) || ((isOsTypeWINDOWS()) && (halfDuplexTXDelay != 0))) {
                //controlRTS(nrOfBytes);

                // wait for tx buffer empty...
                long returnTime = System.currentTimeMillis() + 10000;
                while (serialPort.txBufCount() > 0) {
                    Thread.sleep(1);
                    if (System.currentTimeMillis() - returnTime > 0) {
                        return;
                    }
                }

                // delay another delay ms...
                if (isOsTypeLINUX()) {
                    long delay = System.currentTimeMillis() + (((nrOfBytes * 10) * 1000) / baudrate) + (Math.abs(halfDuplexTXDelay) - 1);
                    while (true) {
                        Thread.sleep(0);
                        if (delay < System.currentTimeMillis()) {
                            break;
                        }
                    }
                } else {
                    long delay = (((nrOfBytes * 10) * 1000) / baudrate) + (Math.abs(halfDuplexTXDelay) - 1);
                    Thread.sleep(delay);
                }

                // drop rts
                if (isOsTypeLINUX() && this.rs485HardwareDriven) {
                    serialPort.setRTS(true);
                } else {
                    serialPort.setRTS(false);
                }
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doServerOpen() throws NestedIOException {
        doOpen();
    }

    @Override
    protected void doOpen() throws NestedIOException {
        if (!boolOpen) {
            SerInputStream serInputStream;
            SerOutputStream serOutputStream;
            try {
                //serialPort = new SerialPortLocal(serialConfig);
                serialPort = SerialPortServiceProvider.getSerialPort(serialConfig);
                serInputStream = new SerInputStream(serialPort);
                serOutputStream = new SerOutputStream(serialPort);
                if (isOsTypeLINUX()) {
                    serOutputStream.setWriteDrain(false);
                }
            } catch (IOException e) {
                throw new NestedIOException(e);
            }

            try {
                // This only to avoid blocking if something goes wrong!!!
                serInputStream.setRcvTimeout(10000);
                setStreams(serInputStream, serOutputStream);
                setParams(baudrate, databits, parity, stopbits);
                flushInputStream(this.getPropertyService().getOpenSerialAndFlush());
                boolOpen = true;
            } catch (NestedIOException e) {
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException ex) {
                    throw new NestedIOException(ex);
                }
                try {
                    serialPort.close();
                } catch (IOException ex) {
                    // absorb
                }
                throw e;
            } catch (Exception e) {
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException ex) {
                    throw new NestedIOException(ex);
                }
                try {
                    serialPort.close();
                } catch (IOException ex) {
                    // absorb
                }
                throw new NestedIOException(e);
            }
        } else {
            throw new NestedIOException(new IOException("Port already open"));
        }

    }

    @Override
    protected void doServerClose() throws NestedIOException {
        doClose();
    }

    @Override
    protected void doClose() throws NestedIOException {
        if (boolOpen) {
            try {
                outputStream.close();
                inputStream.close();
                serialPort.close();
                serialPort = null;
                boolOpen = false;
            } catch (Exception e) {
                throw new NestedIOException(e);
            }
        } else {
            throw new NestedIOException(new IOException("Serial port is not open"));
        }
    }

    @Override
    protected SerialPort doGetSerialPort() {
        return serialPort;
    }

    @Override
    public Socket getSocket() {
        return null;
    }

    @Override
    public UDPSession getUdpSession() {
        return null;
    }

}