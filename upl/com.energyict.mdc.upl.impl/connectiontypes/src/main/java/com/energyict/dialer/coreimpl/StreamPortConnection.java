/*
 * SocketStreamConnection
 *
 * Created on 8 april 2004, 14:08
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.upl.RuntimeEnvironment;

import com.energyict.dialer.serialserviceprovider.SerialPort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Koen
 */
public abstract class StreamPortConnection extends StreamConnectionImpl {

    private static final Log logger = LogFactory.getLog(StreamPortConnection.class);

    // socket for IP communication
    protected Socket socket;
    protected String ipPort;
    protected UDPSession udpSession;
    protected int halfDuplex = 0;
    boolean boolVirtualOpen = false;

    public StreamPortConnection(String ipPort, RuntimeEnvironment runtimeEnvironment) {
        super(runtimeEnvironment);
        this.ipPort = ipPort;
        boolOpen = false;
    }

    // KV 03102005

    public StreamPortConnection(Socket socket, RuntimeEnvironment runtimeEnvironment) {
        super(runtimeEnvironment);
        this.socket = socket;
        boolOpen = false;
    }

    public StreamPortConnection(UDPSession udpSession, RuntimeEnvironment runtimeEnvironment) {
        super(runtimeEnvironment);
        this.udpSession = udpSession;
        boolOpen = false;
    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    protected void doSetParams(int baudrate, int databits, int parity, int stopbits) throws IOException {
        // send escape commands to set parameters
        if (strComPort != null) {
            if (getOutputStream() != null) {
                String escapeSequence = "<ESC>" + COMPORT_SET_PARAMS + "=" + baudrate + "," + databits + "," + parity + "," + stopbits + "</ESC>";
                getOutputStream().write(escapeSequence.getBytes());
            }
        }

    }

    @Override
    protected void doSetComPort(String strComPort) {
        // use strcomPort...
        // send escape commands
        // <ESC>comPort=COM1,...</ESC>
    }

    private boolean getResponse() throws IOException {
        int count = 0;
        while (true) {
            if (getInputStream().available() > 0) {
                int kar = getInputStream().read();
                return kar != 0;
            } else {
                if (count++ > 500) {
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        throw new IOException("SocketStreamConnection, getResponse() timeout...");
    }

    @Override
    protected boolean doSigCD() throws IOException {
        String escapeSequence = "<ESC>" + COMPORT_SIG_CD + "</ESC>";
        getOutputStream().write(escapeSequence.getBytes());
        return getResponse();
    }

    @Override
    protected boolean doSigCTS() throws IOException {
        String escapeSequence = "<ESC>" + COMPORT_SIG_CTS + "</ESC>";
        getOutputStream().write(escapeSequence.getBytes());
        return getResponse();
    }

    @Override
    protected boolean doSigDSR() throws IOException {
        String escapeSequence = "<ESC>" + COMPORT_SIG_DSR + "</ESC>";
        getOutputStream().write(escapeSequence.getBytes());
        return getResponse();
    }

    @Override
    protected boolean doSigRing() throws IOException {
        String escapeSequence = "<ESC>" + COMPORT_SIG_RI + "</ESC>";
        getOutputStream().write(escapeSequence.getBytes());
        return getResponse();
    }

    private boolean sigCTS(boolean state) throws IOException {
        String escapeSequence = "<ESC>" + COMPORT_SIG_CTS + "=" + state + "</ESC>";
        getOutputStream().write(escapeSequence.getBytes());
        boolean response = getResponse();
        if (response) {
            return true;
        } else {
            throw new IOException("SocketStreamConnection, sigCTS(" + state + ") timeout...");
        }
    }

    @Override
    protected void doSetDTR(boolean dtr) throws IOException {
        // send escape commands
        // <ESC>setDTR=true</ESC>
        String escapeSequence = "<ESC>" + COMPORT_SET_DTR + "=" + dtr + "</ESC>";
        getOutputStream().write(escapeSequence.getBytes());
    }

    @Override
    protected void doSetRTS(boolean rts) throws IOException {
        // send escape commands
        // <ESC>setRTS=true</ESC>
        String escapeSequence = "<ESC>" + COMPORT_SET_RTS + "=" + rts + "</ESC>";
        getOutputStream().write(escapeSequence.getBytes());
    }

    @Override
    protected void doRequest2Send(int nrOfBytes) {
        try {
            if (halfDuplex == 0) {
                halfDuplex = (int) halfDuplexTXDelay;
                String escapeSequence = "<ESC>" + COMPORT_SET_HALFDUPLEX + "=" + halfDuplexTXDelay + "</ESC>";
                getOutputStream().write(escapeSequence.getBytes());
            }
            String escapeSequence = "<ESC>" + COMPORT_SEND_LENGTH + "=" + nrOfBytes + "</ESC>";
            getOutputStream().write(escapeSequence.getBytes());
            doSetRTS(true);
            sigCTS(true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doRequest2Receive(int nrOfBytes) {
        // rts set to false is done in the virtual com port! Yes, otherwise we encounter lots of timing problems!!
    }

    @Override
    protected void doRequest2SendV25(int nrOfBytes) {
        try {
            if (halfDuplex == 0) {
                halfDuplex = (int) halfDuplexTXDelay;
                String escapeSequence = "<ESC>" + COMPORT_SET_HALFDUPLEX + "=" + halfDuplexTXDelay + "</ESC>";
                getOutputStream().write(escapeSequence.getBytes());
            }
            String escapeSequence = "<ESC>" + COMPORT_SEND_LENGTH + "=" + nrOfBytes + "</ESC>";
            getOutputStream().write(escapeSequence.getBytes());
            doSetRTS(true);
            sigCTS(true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doRequest2ReceiveV25(int nrOfBytes) {
        // rts set to false is done in the virtual com port! Yes, otherwise we encounter lots of timing problems!!
    }

    @Override
    protected void doRequest2SendRS485() {
        try {
            if (halfDuplex == 0) {
                halfDuplex = (int) halfDuplexTXDelay;
                String escapeSequence = "<ESC>" + COMPORT_SET_HALFDUPLEX + "=" + halfDuplexTXDelay + "</ESC>";
                getOutputStream().write(escapeSequence.getBytes());
            }
            doSetRTS(true);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doRequest2ReceiveRS485(int nrOfBytes) {
        // rts set to false is done in the virtual com port! Yes, otherwise we encounter lots of timing problems!!
    }

    // TODO evt een createVirtual bijmaken om de baudrate te zetten voor de open...

    protected void openVirtual() throws IOException {
        if (!boolVirtualOpen) {
            // use escape sequences to open COMx port remote
            String escapeSequence = "<ESC>" + COMPORT + "=" + strComPort + ".open</ESC>";
            getOutputStream().write(escapeSequence.getBytes());
            //setParams(baudrate,databits, parity, stopbits); // KV 12012006 removed...
            boolVirtualOpen = true;
        }
    }

    protected void closeVirtual() throws IOException {
        if (boolVirtualOpen) {
            // use escape sequences to close COMx port remote
            String escapeSequence = "<ESC>" + COMPORT + "=" + strComPort + ".close</ESC>";
            getOutputStream().write(escapeSequence.getBytes());
            boolVirtualOpen = false;
        }
    }

    @Override
    protected SerialPort doGetSerialPort() {
        return null;
    }

}