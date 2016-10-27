/*
 * ListenerImpl.java
 *
 * Created on 27 mei 2005, 15:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.coreimpl;


import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.Listener;
import com.energyict.dialer.core.ListenerMarker;

import java.io.IOException;


/**
 * @author Koen
 */
abstract public class ListenerImpl extends LinkImpl implements Listener {

    abstract protected void doAccept(int iTimeout, int nrOfRings) throws IOException, LinkException;

    abstract protected void doDisConnect() throws IOException, LinkException;

    abstract protected void doDisConnectServer() throws IOException, LinkException;

    abstract protected void doConnect() throws IOException, LinkException;

    /*  connectionString:
    *  TCP1 (TCP port)
    *  COM1 (local COM port)
    *  10.0.0.28:23 (IP transparant connection)
    *  10.0.0.28:23:COM1 (Virtual COM port)
    */

    /**
     * Creates a new instance of DialerImpl
     */
    public ListenerImpl() {
    }


//    public void setStreamObservers(Object obj) {
//        inputStreamObserver = (InputStreamObserver)obj;
//        outputStreamObserver = (OutputStreamObserver)obj;
//    }

    public void accept() throws IOException, LinkException {
        accept(60000);
    }

    public void accept(int timeout) throws IOException, LinkException {
        accept(timeout, 1);
    }

    public void accept(int timeout, int nrOfRings) throws IOException, LinkException {
        if ((!ListenerMarker.hasIPListenMarker(this)) && (!ListenerMarker.hasUDPListenMarker(this))) {
            if ((getStreamConnection() != null) && (connectionString != null)) {
                getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
                getStreamConnection().open();
            }
        }
        doAccept(timeout, nrOfRings);
    }

    public void connect() throws IOException, LinkException {
        if ((!ListenerMarker.hasIPListenMarker(this)) && (!ListenerMarker.hasUDPListenMarker(this))) {
            if ((getStreamConnection() != null) && (connectionString != null)) {
                getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
                getStreamConnection().open();
            }
        }
        doConnect();
    }

    public void disConnect() throws IOException, LinkException {
        try {
            doDisConnect();
        } finally {
            if ((getStreamConnection() != null) && (connectionString != null)) {
                getStreamConnection().close();
            }
        }
    }

    public void disConnectServer() throws IOException, LinkException {
        try {
            doDisConnectServer();
        } finally {
            if ((getStreamConnection() != null) && (connectionString != null)) {
                getStreamConnection().serverClose();
            }
        }
    }
}
