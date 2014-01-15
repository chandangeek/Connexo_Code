/*
 * DirectDialer.java
 *
 * Created on 13 april 2004, 9:21
 */

package com.energyict.dialer.core.impl;

import com.energyict.mdc.protocol.api.dialer.core.DialerException;
import com.energyict.mdc.protocol.api.dialer.core.Direct;
import com.energyict.mdc.common.NestedIOException;

/**
 * @author Koen
 */
public class DirectDialer extends DialerImpl implements Direct {

    /**
     * Creates a new instance of DirectDialer
     */
    public DirectDialer() {
    }

    protected void doConnect(String strDialAddress1, String strDialAddress2, int iTimeout) throws NestedIOException, DialerException {
    }

    protected void doDisConnect() throws NestedIOException, DialerException {
    }

}
