/*
 * Listener.java
 *
 * Created on 27 mei 2005, 15:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.core;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.Link;
import com.energyict.mdc.protocol.api.dialer.core.LinkException;
import com.energyict.mdc.protocol.api.dialer.core.StreamConnection;

import java.io.IOException;

/**
 * @author Koen
 */
public interface Listener extends Link {

    /**
     * This method waits and accepts an incoming connection request from a remote device. Timeout to wait
     * for CONNECT after the call has been answered is 60 sec.
     *
     * @throws ListenerException When there is a specific dialer error.
     * @throws NestedIOException All other exceptions
     */
    public void accept() throws IOException, LinkException;

    /**
     * This method waits and accepts an incoming connection request from a remote device.
     *
     * @param iTimeout Timeout for a connection to establish after the incoming call has been answered
     * @throws ListenerException When there is a specific dialer error.
     * @throws NestedIOException All other exceptions
     */
    public void accept(int iTimeout) throws IOException, LinkException;

    /**
     * This method waits and accepts an incoming connection request from a remote device.
     *
     * @param iTimeout  Timeout for a connection to establish after the incoming call has been answered
     * @param nrOfRings nrOfRings to receive in case of an ATListener dialin
     * @throws ListenerException When there is a specific dialer error.
     * @throws NestedIOException All other exceptions
     */
    public void accept(int iTimeout, int nrOfRings) throws IOException, LinkException;

    /**
     * @throws IOException
     * @throws LinkException
     */
    public void disConnectServer() throws IOException, LinkException;

    /**
     * @return
     */
    public StreamConnection getAcceptedStreamConnection();

}
