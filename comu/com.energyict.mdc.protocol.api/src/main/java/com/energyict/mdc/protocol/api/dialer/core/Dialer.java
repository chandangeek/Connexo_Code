package com.energyict.mdc.protocol.api.dialer.core;

/*
 * Dialer.java
 *
 * Created on 13 april 2004, 9:36
 */

import com.energyict.mdc.common.NestedIOException;

import java.io.IOException;

/**
 * @author Koen
 */
public interface Dialer extends Link {

    /**
     * This method set up the connection with the remote device.
     *
     * @param strDialAddress1 address to dial (in case of ATDialer it is a phone number, in case of X25 it is a base address, ...).
     * @param iTimeout        Timeout for a connection to establish.
     * @throws DialerException   When there is a specific dialer error.
     * @throws NestedIOException All other exceptions
     */
    void connect(String strDialAddress1, int iTimeout) throws IOException, LinkException;

    /**
     * This method set up the connection with the remote device.
     *
     * @param strDialAddress1 address to dial (in case of ATDialer it is a phone number, in case of X25 it is a base address, ...).
     * @param strDialAddress2 second address to dial (in case of PEMP dialer).
     * @param iTimeout        Timeout for a connection to establish.
     * @throws DialerException When there is a specific dialer error.
     */
    void connect(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException;

}