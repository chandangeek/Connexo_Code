/*
 * DialerImpl.java
 *
 * Created on 13 april 2004, 10:05
 */

package com.energyict.dialer.coreimpl;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.StreamConnection;

import java.io.IOException;


/**
 * @author Koen
 */
public abstract class DialerImpl extends LinkImpl implements Dialer {

    protected abstract void doConnect(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException;

    protected abstract void doDisConnect() throws IOException, LinkException;

    // forward a dialer
    Dialer dialer = null;
    String dialAddr1 = null;
    String dialAddr2 = null;
    int timeout = 0;

    /*  connectionString:
    *  TCP1 (TCP port)
    *  COM1 (local COM port)
    *  10.0.0.28:23 (IP transparant connection)
    *  10.0.0.28:23:COM1 (Virtual COM port)
    */

    /**
     * Creates a new instance of DialerImpl
     */
    public DialerImpl() {
    }


    public void connectDialer(Dialer dialer) {
        connectDialer(dialer, null);
    }

    public void connectDialer(Dialer dialer, String dialAddr1) {
        connectDialer(dialer, dialAddr1, null);
    }

    public void connectDialer(Dialer dialer, String dialAddr1, String dialAddr2) {
        connectDialer(dialer, dialAddr1, dialAddr2, 60000);
    }

    public void connectDialer(Dialer dialer, String dialAddr1, String dialAddr2, int timeout) {
        this.dialer = dialer;
        this.dialAddr1 = dialAddr1;
        this.dialAddr2 = dialAddr2;
        if (timeout == 0) {
            timeout = 60000;
        }
        this.timeout = timeout;
    }

    public void connect() throws IOException, LinkException {
        connect(null, null, 0);
    }

    public void connect(String strDialAddress1, int iTimeout) throws IOException, LinkException {
        connect(strDialAddress1, null, iTimeout);
    }

    public void connect(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException {
        if ((getStreamConnection() != null) && (connectionString != null)) {
            getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
            getStreamConnection().open();
        }
        if (iTimeout == 0) {
            iTimeout = 60000;
        }
        doConnect(strDialAddress1, strDialAddress2, iTimeout);
        if (dialer != null) {
            setStreamConnection(getStreamConnection());
            dialer.connect(dialAddr1, dialAddr2, timeout);
        }

    }

    public void disConnect() throws IOException, LinkException {
        try {
            if (dialer != null) {
                dialer.disConnect();
            }

            doDisConnect();
        } finally {
            if ((getStreamConnection() != null) && (connectionString != null)) {
                getStreamConnection().close();
            }
        }
    }


    /**
     * Update the StreamConnection so proper monitoring can be performed by the ComServer
     *
     * @param streamConnection the <i>new</i> StreamConnection
     */
    public void updateStreamConnection(StreamConnection streamConnection) {
        setStreamConnection(streamConnection);
        getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
    }
}
