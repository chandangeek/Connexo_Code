/*
 * <p>Title: DialerTimeoutException </p>
 * <p>Created on 13 april 2004, 9:44</p>
 * <p>Description: Subclass for exceptions specific to the dialer connection timeout </p>
 * <p>Changes:</P>
 * <p>KV 13042004 initial version </p>
 */

package com.energyict.mdc.protocol.api.dialer.core;

import com.energyict.mdc.protocol.api.dialer.core.DialerException;

/**
 * @author Koen
 */
public class ListenerTimeoutException extends DialerException {

    /**
     * Creates a new instance of DialerTimeoutException
     */
    public ListenerTimeoutException(String str) {
        super(str);
    }

    public ListenerTimeoutException() {
        super();
    }

}