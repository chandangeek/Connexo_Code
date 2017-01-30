package com.energyict.mdc.upl.io;

import java.io.Closeable;
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
public interface VirtualUdpSession extends Closeable {

    /**
     * Provides the used DatagramSocket for this session.
     *
     * @return the DatagramSocket
     */
    DatagramSocket getDatagramSocket();

    /**
     * Provides the used inputStream for this session.
     *
     * @return the InputStream
     */
    InputStream getInputStream();

    /**
     * Provides the used outputStream for this session.
     *
     * @return the OutputStream
     */
    OutputStream getOutputStream();

}
