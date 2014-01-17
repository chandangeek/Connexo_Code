/*
 * StreamConnection.java
 *
 * Created on 13 april 2004, 9:34
 */

package com.energyict.mdc.protocol.api.dialer.core;

import com.energyict.mdc.common.NestedIOException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Koen
 */
public interface StreamConnection extends SerialCommunicationChannel {

    public static final String COMPORT = "ComPort";
    public static final String COMPORT_SET_HALFDUPLEX = "ComPortSetHalfDuplex";
    public static final String COMPORT_SEND_LENGTH = "ComPortSendLength";
    public static final String COMPORT_SET_PARAMS = "ComPortSetParams";
    public static final String COMPORT_SET_DTR = "ComPortSetDTR";
    public static final String COMPORT_SET_RTS = "ComPortSetRTS";
    public static final String COMPORT_SIG_CTS = "ComPortGetCTS";
    public static final String COMPORT_SIG_DSR = "ComPortGetDSR";
    public static final String COMPORT_SIG_RI = "ComPortGetRI";
    public static final String COMPORT_SIG_CD = "ComPortGetCD";
    public static final String COMPORT_SIG_EMPTY_TXBUFF = "ComPortTxBuffEmpty";

    public InputStream getInputStream();

    public OutputStream getOutputStream();

    public void setStreams(InputStream is, OutputStream os);

    public void flushInputStream(long delay) throws IOException;

    public void setStreamObservers(InputStreamObserver iso, OutputStreamObserver oso);

    public void open() throws NestedIOException;

    public void serverOpen() throws NestedIOException;

    public void accept() throws NestedIOException;

    public void accept(int timeOut) throws NestedIOException;

    public void close() throws NestedIOException;

    public void serverClose() throws NestedIOException;

    public boolean isOpen();

    public void write(String strData, int iTimeout) throws IOException;

    public void write(String strData) throws IOException;

    public Socket getSocket();

    public UDPSession getUdpSession();

}