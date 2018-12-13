package com.energyict.dialer.serialserviceprovider.rxtx;


import com.energyict.dialer.uplserialserviceprovider.SerialConfig;
import com.energyict.dialer.uplserialserviceprovider.SerialPort;
import com.energyict.mdc.upl.io.NestedIOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

public class SerialPortImpl implements SerialPort, gnu.io.SerialPortEventListener {

    private static final Log logger = LogFactory.getLog(SerialPortImpl.class);


    private gnu.io.SerialPort rxtxSerialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean outputBufferEmpty = false;

    public SerialPortImpl() {
    }

    public void init(SerialConfig serialConfig) throws IOException {

        if (logger.isDebugEnabled()) {
            logger.debug("RXTX init for " + serialConfig.getComPortStr());
        }
        gnu.io.CommPort commPort;
        try {
            //System.out.println("RXTX serial driver\n"+"library: "+gnu.io.RXTXVersion.getVersion()+"\n"+"native dll: "+gnu.io.RXTXVersion.nativeGetVersion());
            commPort = gnu.io.CommPortIdentifier.getPortIdentifier(serialConfig.getComPortStr()).open(this.getClass().getName(), 2000);
        } catch (gnu.io.PortInUseException | gnu.io.NoSuchPortException e) {
            throw new NestedIOException(e);
        }
        if (commPort instanceof gnu.io.SerialPort) {
            rxtxSerialPort = (gnu.io.SerialPort) commPort;
            try {
                rxtxSerialPort.notifyOnOutputEmpty(true);
                rxtxSerialPort.addEventListener(this);

            } catch (TooManyListenersException e) {
                throw new NestedIOException(e);
            }
            setParams(serialConfig);
            inputStream = rxtxSerialPort.getInputStream();
            outputStream = rxtxSerialPort.getOutputStream();
        } else {
            throw new IOException("rxtx error: Only serial ports are handled by this example.");
        }
    }

    private void setParams(SerialConfig serialConfig) throws IOException {

        int baudIndex = serialConfig.getBitRate();
        int databits = serialConfig.getDataBits();
        int parity = serialConfig.getParity();
        int stopbits = serialConfig.getStopBits();

        int baudrate = 9600; // default
        if (baudIndex == 2) {
            baudrate = 300;
        } else if (baudIndex == 3) {
            baudrate = 600;
        } else if (baudIndex == 4) {
            baudrate = 1200;
        } else if (baudIndex == 5) {
            baudrate = 2400;
        } else if (baudIndex == 6) {
            baudrate = 4800;
        } else if (baudIndex == 7) {
            baudrate = 9600;
        } else if (baudIndex == 8) {
            baudrate = 19200;
        } else if (baudIndex == 9) {
            baudrate = 38400;
        } else if (baudIndex == 10) {
            baudrate = 57600;
        } else if (baudIndex == 11) {
            baudrate = 115200;
        } else {
            throw new IOException("rxtx SerialPortImpl, doSetParams, invalid baudrate index " + baudIndex);
        }

        if (rxtxSerialPort != null) {
            try {
                rxtxSerialPort.setSerialPortParams(baudrate, databits + 5, stopbits + 1, parity);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                throw new NestedIOException(e);
            }
        }

    } // protected void doSetParams(int baudrate,int databits, int parity, int stopbits) throws IOException

    public void close() throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("RXTX close " + rxtxSerialPort.getName());
        }
        rxtxSerialPort.close();
    }

    public void configure(SerialConfig serialConfig) throws IOException {
        setParams(serialConfig);
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setDTR(boolean dtr) throws IOException {
        rxtxSerialPort.setDTR(dtr);
    }

    public void setRTS(boolean rts) throws IOException {
        rxtxSerialPort.setRTS(rts);
    }

    public boolean sigCD() throws IOException {
        return rxtxSerialPort.isCD();
    }

    public boolean sigCTS() throws IOException {
        return rxtxSerialPort.isCTS();
    }

    public boolean sigDSR() throws IOException {
        return rxtxSerialPort.isDSR();
    }

    public boolean sigRing() throws IOException {
        return rxtxSerialPort.isRI();
    }

    public int txBufCount() throws IOException {
        int txBufCount = outputBufferEmpty ? 0 : 1;
        setOutputBufferEmpty(false);
        return txBufCount;
    }

    public void serialEvent(gnu.io.SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == gnu.io.SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
            setOutputBufferEmpty(true);
        }
    }

    private synchronized void setOutputBufferEmpty(boolean outputBufferEmpty) {
        this.outputBufferEmpty = outputBufferEmpty;
    }

    public void setRcvTimeout(int receiveTimeout) throws IOException {
        try {
            rxtxSerialPort.enableReceiveTimeout(receiveTimeout);
        } catch (gnu.io.UnsupportedCommOperationException e) {
            throw new NestedIOException(e); //e.printStackTrace();
        }
    }

    public void setWriteDrain(boolean writeDrain) {
        // TODO ?? not supported in rxtx ??

    }
}