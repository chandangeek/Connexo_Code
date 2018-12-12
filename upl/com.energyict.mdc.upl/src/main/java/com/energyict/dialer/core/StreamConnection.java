/*
 * StreamConnection.java
 *
 * Created on 13 april 2004, 9:34
 */

package com.energyict.dialer.core;

import com.energyict.protocol.tools.InputStreamObserver;
import com.energyict.protocol.tools.OutputStreamObserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Koen
 */
public interface StreamConnection extends SerialCommunicationChannel {

    String COMPORT = "ComPort";
    String COMPORT_SET_HALFDUPLEX = "ComPortSetHalfDuplex";
    String COMPORT_SEND_LENGTH = "ComPortSendLength";
    String COMPORT_SET_PARAMS = "ComPortSetParams";
    String COMPORT_SET_DTR = "ComPortSetDTR";
    String COMPORT_SET_RTS = "ComPortSetRTS";
    String COMPORT_SIG_CTS = "ComPortGetCTS";
    String COMPORT_SIG_DSR = "ComPortGetDSR";
    String COMPORT_SIG_RI = "ComPortGetRI";
    String COMPORT_SIG_CD = "ComPortGetCD";
    String COMPORT_SIG_EMPTY_TXBUFF = "ComPortTxBuffEmpty";

    InputStream getInputStream();

    OutputStream getOutputStream();

    void setStreams(InputStream is, OutputStream os);

    void flushInputStream(long delay) throws IOException;

    void setStreamObservers(InputStreamObserver iso, OutputStreamObserver oso);

    void open() throws IOException;

    void serverOpen() throws IOException;

    void accept() throws IOException;

    void accept(int timeOut) throws IOException;

    void close() throws IOException;

    void serverClose() throws IOException;

    boolean isOpen();

    void write(String strData, int iTimeout) throws IOException;

    void write(String strData) throws IOException;

    Socket getSocket();

}