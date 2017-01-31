/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.dialer.core;

/*
 * CommunicationPostSettings.java
 *
 * Created on 17 september 2003, 9:07
 */

import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialPort;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Koen
 */
public interface SerialCommunicationChannel extends HalfDuplexController {

    // parity settings
    int PARITY_NONE = 0;
    int PARITY_ODD = 1;
    int PARITY_EVEN = 2;
    int PARITY_MARK = 3;
    int PARITY_SPACE = 4;
    // stopbits settings
    int STOPBITS_1 = 1;
    int STOPBITS_2 = 2;
    int STOPBITS_1_5 = 3;
    // databits settings
    int DATABITS_8 = 8;
    int DATABITS_7 = 7;
    int DATABITS_6 = 6;
    int DATABITS_5 = 5;

    /**
     * setParams(). Set the communication parameters for the open port.
     *
     * @param baudrate : 300,1200,2400,4800,9600,19200,...
     * @param databits : SerialPort.DATABITS_x (x=8,7,6,5)
     * @param parity   : SerialPort.PARITY_x (x=NONE (0),EVEN (2),ODD (1) ,MARK(3),SPACE (4))
     * @param stopbits : SerialPort.STOPBITS_x (x=1 (1),2 (2),1_5 (3))
     * @throws IOException
     */
    void setParams(int baudrate, int databits, int parity, int stopbits) throws IOException;

    void setParity(int databits, int parity, int stopbits) throws IOException;

    void setParityAndFlush(int databits, int parity, int stopbits) throws IOException;

    void setParamsAndFlush(int baudrate, int databits, int parity, int stopbits) throws IOException;

    void setBaudrate(int baudrate) throws IOException;

    void setBaudrateAndFlush(int baudrate) throws IOException;

    String getComPort();

    void setComPort(String strComPort);

    boolean sigRing() throws IOException;

    boolean sigDSR() throws IOException;

    SerialPort getSerialPort();

    InputStream getInputStream();

    OutputStream getOutputStream();

}