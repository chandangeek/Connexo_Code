/*
 * <p>Title: DialerCarrierException </p>
 * <p>Created on 13 april 2004, 9:44</p>
 * <p>Description: Subclass for exceptions specific to the dialer carrier </p>
 * <p>Changes:</P>
 * <p>KV 13042004 initial version </p>
 */

package com.energyict.dialer.core.impl;

import com.energyict.mdc.protocol.api.dialer.core.DialerException;

/**
 * @author Koen
 */
public class DialerCarrierException extends DialerException {

    /**
     * Creates a new instance of DialerTimeoutException
     *
     * @param message The exception message
     */
    public DialerCarrierException(String message) {
        super(message);
    }

    /**
     * Default constructor for the {@link DialerCarrierException}
     */
    public DialerCarrierException() {
        super();
    }

}