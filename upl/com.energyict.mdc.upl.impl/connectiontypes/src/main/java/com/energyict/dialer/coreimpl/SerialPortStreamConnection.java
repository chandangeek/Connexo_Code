/*
 * SerialPortStreamConnection.java
 *
 * Created on 13 april 2004, 10:41
 */

package com.energyict.dialer.coreimpl;

import com.energyict.cbo.NestedIOException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.serialserviceprovider.SerInputStream;
import com.energyict.dialer.serialserviceprovider.SerOutputStream;
import com.energyict.dialer.serialserviceprovider.SerialConfig;
import com.energyict.dialer.serialserviceprovider.SerialPort;
import com.energyict.dialer.serialserviceprovider.SerialPortServiceProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * @author Koen
 */
public class SerialPortStreamConnection extends StreamConnectionImpl {

    private static final Log logger = LogFactory.getLog(SerialPortStreamConnection.class);

    /**
     * This is a property that can be set in eiserver.properties. True indicates that the RS485 RTS is managed in SW (as
     * with a 8250 UART), false or missing means that the RTS is managed in HW (as with an RTU+Server, which uses an Exar chipset
     * for doing this.
     */
    private static final String PROPERTY_RS485_SOFTWARE_DRIVEN = "dialer.serialport.rs485.softwaredriven";

    // Serial service provider objects for serial port communication
    private SerialConfig serialConfig = null;
    private SerialPort serialPort = null;

    /**
     * Indicates whether the RS485 is software driven or not. This only matters on Linux, where we have the RTU+Server using Exar
     * ports that have hardware driven RS485 (which is the default), but a development machine using a standard 8250 UART has to do
     * software half duplex, so in that case the eiserver.properties needs to contain {@link #PROPERTY_RS485_SOFTWARE_DRIVEN} and it
     * has to be set to true, otherwise you will never receive any response from the device.
     */
    private final boolean rs485SoftwareDriven;

    /**
     * Creates a new instance of SerialPortStreamConnection
     */
    public SerialPortStreamConnection(String strComPort) {
        setComPort(strComPort);

        boolOpen = false;

        this.rs485SoftwareDriven = Boolean.valueOf(Environment.getDefault().getProperty(PROPERTY_RS485_SOFTWARE_DRIVEN, "false"));
    }

    //****************************************************************************
    // Delegate of implementation of interface SerialCommunicationChannel
    //****************************************************************************

    /**
     * doSetParams().
     * Set the communication parameters for the open port.
     *
     * @param iBaudrate : 300,1200,2400,4800,9600,19200,...
     * @param iDatabits : SerialPort.DATABITS_x (x=8,7,6,5)
     * @param iParity   : SerialPort.PARITY_x (x=NONE (0),EVEN (2),ODD (1) ,MARK (3),SPACE (4))
     * @param iStopbits : SerialPort.STOPBITS_x (x=1 (1),2 (2),1_5 (3))
     * @throws IOException
     */
    protected void doSetParams(int baudrate, int databits, int parity, int stopbits) throws IOException {
        int baudIndex;
        if (baudrate == 300) {
            baudIndex = 2;
        } else if (baudrate == 600) {
            baudIndex = 3;
        } else if (baudrate == 1200) {
            baudIndex = 4;
        } else if (baudrate == 2400) {
            baudIndex = 5;
        } else if (baudrate == 4800) {
            baudIndex = 6;
        } else if (baudrate == 9600) {
            baudIndex = 7;
        } else if (baudrate == 19200) {
            baudIndex = 8;
        } else if (baudrate == 38400) {
            baudIndex = 9;
        } else if (baudrate == 57600) {
            baudIndex = 10;
        } else if (baudrate == 115200) {
            baudIndex = 11;
        } else {
            throw new IOException("SerialPortConnection, doSetParams, invalid baudrate " + baudrate);
        }

        serialConfig.setBitRate(baudIndex); //SerialConfig.BR_19200);
        serialConfig.setDataBits(databits - 5); //SerialConfig.LN_8BITS);
        serialConfig.setStopBits(stopbits - 1); //SerialConfig.LN_1BITS);
        serialConfig.setParity(parity); //SerialConfig.PY_NONE);

        if (serialPort != null) {
            serialPort.configure(serialConfig);
        }
    } // protected void doSetParams(int baudrate,int databits, int parity, int stopbits) throws IOException

    protected void doSetComPort(String strComPort) {
        serialConfig = new SerialConfig(strComPort);
    }

    private boolean isIgnoreDCD() {
        String ignoreDCDComPorts = Environment.getDefault().getProperty("ignoreDCDComPorts", null);
        if (ignoreDCDComPorts == null) {
            return false;
        }
        try {
            StringTokenizer strTok = new StringTokenizer(ignoreDCDComPorts, ",");
            while (strTok.hasMoreTokens()) {
                if (getComPort().compareTo(strTok.nextToken()) == 0) {
                    return true;
                }
            }
            return false;
        } catch (NumberFormatException ex) {
            // silently ignore
            return false;
        }
    }

    protected boolean doSigCD() throws IOException {
        if (isIgnoreDCD()) {
            return false;
        } else {
            return serialPort.sigCD();
        }
    }

    protected boolean doSigCTS() throws IOException {
        return serialPort.sigCTS();
    }

    protected boolean doSigDSR() throws IOException {
        return serialPort.sigDSR();
    }

    protected boolean doSigRing() throws IOException {
        return serialPort.sigRing();
    }

    protected void doSetDTR(boolean dtr) throws IOException {
        serialPort.setDTR(dtr);
    }

    protected void doSetRTS(boolean rts) throws IOException {
        serialPort.setRTS(rts);
    }


    //****************************************************************************************
    // Delegate of implementation of interface HalfDuplexController
    //****************************************************************************************

    protected void doRequest2Send(int nrOfBytes) {
        try {
            serialPort.setRTS(true);
            long returnTime = System.currentTimeMillis() + 2000;
            while (true) {
                Thread.sleep(50);
                if (serialPort.sigCTS() == true) {
                    return;
                }
                if (((long) (System.currentTimeMillis() - returnTime)) > 0) {
                    return;
                }
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void doRequest2Receive(int nrOfBytes) {
        try {


            // wait for tx buffer empty...
            long returnTime = System.currentTimeMillis() + 10000;
            while (serialPort.txBufCount() > 0) {
                Thread.sleep(100);
                if (((long) (System.currentTimeMillis() - returnTime)) > 0) {
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
                if (serialPort.sigCTS() == false) {
                    return;
                }
                if (((long) (System.currentTimeMillis() - returnTime)) > 0) {
                    return;
                }
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    } // protected void doRequest2Receive(int nrOfBytes)


    //****************************************************************************************
    // Delegate of implementation of interface HalfDuplexController
    //****************************************************************************************

    protected void doRequest2SendV25(int nrOfBytes) {
        try {

            // wait for CD false
            long returnTime = System.currentTimeMillis() + 2000;
            while (true) {
                Thread.sleep(10);
                if (serialPort.sigCD() == false) {
                    break;
                }
                if (((long) (System.currentTimeMillis() - returnTime)) > 0) {
                    break;
                }
            }

            // raise RTS
            serialPort.setRTS(true);

            // wait for CTS true
            returnTime = System.currentTimeMillis() + 2000;
            while (true) {
                Thread.sleep(50);
                if (serialPort.sigCTS() == true) {
                    return;
                }
                if (((long) (System.currentTimeMillis() - returnTime)) > 0) {
                    return;
                }
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    } // protected void doRequest2SendV25(int nrOfBytes)

    protected void doRequest2ReceiveV25(int nrOfBytes) {
        try {

            // wait for tx buffer empty...
            long returnTime = System.currentTimeMillis() + 10000;
            while (serialPort.txBufCount() > 0) {
                Thread.sleep(100);
                if (((long) (System.currentTimeMillis() - returnTime)) > 0) {
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
                if (serialPort.sigCD() == true) {
                    break;
                }
                if (((long) (System.currentTimeMillis() - returnTime)) > 0) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    } // protected void doRequest2ReceiveV25(int nrOfBytes)


    //****************************************************************************************
    // Delegate of implementation of interface HalfDuplexController
    //****************************************************************************************

    protected void doRequest2SendRS485() {
        try {
            // if halfDuplexTXDelay>0, use software RST control
            if ((halfDuplexTXDelay > 0) || ((isOsTypeWINDOWS()) && (halfDuplexTXDelay != 0))) {
                if (isOsTypeLINUX() && !this.rs485SoftwareDriven) {
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
            logger.error(e.getMessage(), e);
        }
    } // protected void doRequest2SendRS485(int nrOfBytes)


    protected void doRequest2ReceiveRS485(int nrOfBytes) {
        try {
            // if halfDuplexTXDelay>0, use software RST control
            if ((halfDuplexTXDelay > 0) || ((isOsTypeWINDOWS()) && (halfDuplexTXDelay != 0))) {
                //controlRTS(nrOfBytes);

                // wait for tx buffer empty...
                long returnTime = System.currentTimeMillis() + 10000;
                while (serialPort.txBufCount() > 0) {
                    Thread.sleep(1);
                    if (((long) (System.currentTimeMillis() - returnTime)) > 0) {
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
                if (isOsTypeLINUX() && !this.rs485SoftwareDriven) {
                    serialPort.setRTS(true);
                } else {
                    serialPort.setRTS(false);
                }
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    } // protected void doRequest2ReceiveRS485(int nrOfBytes)


    final int OPEN_SERIAL_AND_FLUSH = getIntProperty("openSerialAndFlush", 500);

    //****************************************************************************************
    // Delegate of implementation of interface StreamConnection
    //****************************************************************************************

    protected void doServerOpen() throws NestedIOException {
        doOpen();
    }

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
                flushInputStream(OPEN_SERIAL_AND_FLUSH);
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

    } // private void doOpen()

    protected void doServerClose() throws NestedIOException {
        doClose();
    }

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

    protected SerialPort doGetSerialPort() {
        return serialPort;
    }

    public Socket getSocket() {
        return null;
    }

    public UDPSession getUdpSession() {
        return null;
    }

    // private void doClose()


} // SerialPortConnection
