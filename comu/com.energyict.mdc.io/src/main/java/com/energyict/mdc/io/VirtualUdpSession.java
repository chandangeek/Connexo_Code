/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;

@ProviderType
public interface VirtualUdpSession extends AutoCloseable {

    /**
     * Provides the used DatagramSocket for this session.
     *
     * @return the DatagramSocket
     */
    public DatagramSocket getDatagramSocket();

    /**
     * Provides the used inputStream for this session.
     *
     * @return the InputStream
     */
    public InputStream getInputStream();

    /**
     * Provides the used outputStream for this session.
     *
     * @return the OutputStream
     */
    public OutputStream getOutputStream();

}