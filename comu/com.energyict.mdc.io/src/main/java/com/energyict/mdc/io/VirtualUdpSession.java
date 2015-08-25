package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;

/**
 * Models the fact that an UDP session has no real session, but want to use an Input- and OutputStream.
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:56
 */
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