package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.dialer.core.ListenerException;
import com.energyict.mdc.protocol.api.dialer.core.StreamConnection;
import com.energyict.dialer.core.UDPListen;

import java.io.IOException;

public class UDPListener extends ListenerImpl implements UDPListen {

    private ServerDatagramConnection sdc = null;

    /**
     * Creates a new instance of IPListener
     */
    public UDPListener() {
    }

    public StreamConnection getAcceptedStreamConnection() {
        return sdc.getAcceptedDatagramConnection();
    }

    protected void doAccept(int timeout, int nrOfRings) throws NestedIOException, ListenerException {
        if (getStreamConnection() == null) {
            sdc = new ServerDatagramConnection(connectionString);
            setStreamConnection(sdc);
            getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
            getStreamConnection().serverOpen();
        }
        getStreamConnection().accept(timeout);
    }

    protected void doConnect() throws IOException, ListenerException {
// misschien een betere oplossing...
//        Socket socket = ((ServerSocketStreamConnection)getStreamConnection()).getSocket();
//        if (socket != null) {
//            setStreamConnection(new SocketStreamConnection(socket));
//            getStreamConnection().setStreamObservers(inputStreamObserver,outputStreamObserver);
//            getStreamConnection().open();
//        }


        if (getStreamConnection() == null) {
            setStreamConnection(new ServerDatagramConnection(connectionString));
            getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
            getStreamConnection().open();
        } else {
            getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
            getStreamConnection().open();
        }
    }

    protected void doDisConnect() throws IOException, ListenerException {

    }

    protected void doDisConnectServer() throws IOException, ListenerException {

    }

}
