package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.core.DialerException;
import com.energyict.dialer.core.IPDial;
import com.energyict.dialer.core.LinkException;

import java.io.IOException;

public class IPDialer extends DialerImpl implements IPDial {

    /**
     * Creates a new instance of IPDialer
     */
    public IPDialer() {
    }

    protected void doConnect(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException {
        setStreamConnection(new SocketStreamConnection(strDialAddress1));
        getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
        getStreamConnection().open();
        dial(strDialAddress1, strDialAddress2, iTimeout);
    }

    protected void doDisConnect() throws NestedIOException, DialerException {
    }

    protected void dial(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException {
        // override in subclasses
    }
}
