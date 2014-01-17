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
     * @throws DialerException   When there is a specific dialer error.
     * @throws NestedIOException All other exceptions
     */
    void connect() throws IOException, LinkException;

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

    /**
     * This method disconnect the connection with the remote device.
     *
     * @throws DialerException   When there is a specific dialer error.
     * @throws NestedIOException All other exceptions
     */
    void disConnect() throws IOException, LinkException;

    /**
     * connectDialer method enables the caller to dial via dialer after a successfull connection
     * To use a 'forward' dialer, create the dialer with a null connectionString.
     *
     * @param dialer the dialer to use to establish a connection
     */
    void connectDialer(Dialer dialer);

    /**
     * connectDialer method enables the caller to dial via dialer after a successfull connection
     * To use a 'forward' dialer, create the dialer with a null connectionString.
     *
     * @param dialer    the dialer to use to establish a connection
     * @param dialAddr1 the first dial string to use with the dialer
     */
    void connectDialer(Dialer dialer, String dialAddr1);

    /**
     * connectDialer method enables the caller to dial via dialer after a successfull connection
     * To use a 'forward' dialer, create the dialer with a null connectionString.
     *
     * @param dialer    the dialer to use to establish a connection
     * @param dialAddr1 the first dial string to use with the dialer
     * @param dialAddr2 the second dial string to use with the dialer
     */
    void connectDialer(Dialer dialer, String dialAddr1, String dialAddr2);

    /**
     * connectDialer method enables the caller to dial via dialer after a successfull connection
     * To use a 'forward' dialer, create the dialer with a null connectionString.
     *
     * @param dialer    the dialer to use to establish a connection
     * @param dialAddr1 the first dial string to use with the dialer
     * @param dialAddr2 the second dial string to use with the dialer
     * @param timeout   the timeout to establish a connection to use with the dialer
     */
    void connectDialer(Dialer dialer, String dialAddr1, String dialAddr2, int timeout);

}
