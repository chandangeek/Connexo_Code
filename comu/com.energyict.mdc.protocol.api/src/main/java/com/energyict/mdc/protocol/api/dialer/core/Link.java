package com.energyict.mdc.protocol.api.dialer.core;

/*
 * Link.java
 *
 * Created on 30 mei 2005, 13:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Koen
 */
public interface Link {

    InputStream getInputStream();

    OutputStream getOutputStream();

    SerialCommunicationChannel getSerialCommunicationChannel();

    HalfDuplexController getHalfDuplexController();

    void init(String connectionString) throws LinkException;

    void init(String connectionString, String strModemInitCommPort) throws LinkException;

    void init(String connectionString, String strModemInitCommPort, String strModemInitExtra) throws LinkException;

    void init(String connectionString, String strModemInitCommPort, String strModemInitExtra, String strDialPrefix) throws LinkException;

    /**
     * This method disconnect the connection with the remote device.
     * KV 03102005
     *
     * @throws LinkException When there is a specific dialer error.
     * @throws IOException   All other exceptions
     */
    void disConnect() throws IOException, LinkException;

    /**
     * This method set up the connection with the remote device.
     * KV 03102005
     *
     * @throws LinkException     When there is a specific dialer error.
     * @throws NestedIOException All other exceptions
     */
    void connect() throws IOException, LinkException;

}