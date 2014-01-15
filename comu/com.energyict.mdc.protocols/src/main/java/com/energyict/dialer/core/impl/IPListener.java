package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.IPListen;
import com.energyict.dialer.core.ListenerException;
import com.energyict.mdc.protocol.api.dialer.core.StreamConnection;

import java.io.IOException;

public class IPListener extends ListenerImpl implements IPListen {

    private ServerSocketStreamConnection sssc = null;

    /**
     * Creates a new instance of IPListener
     */
    public IPListener() {
    }

    public StreamConnection getAcceptedStreamConnection() {
        return sssc.getAcceptedSocketStreamConnection();
    }

    protected void doAccept(int timeout, int nrOfRings) throws NestedIOException, ListenerException {
        if (getStreamConnection() == null) {
            sssc = new ServerSocketStreamConnection(connectionString);
            setStreamConnection(sssc);
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
            setStreamConnection(new ServerSocketStreamConnection(connectionString));
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
