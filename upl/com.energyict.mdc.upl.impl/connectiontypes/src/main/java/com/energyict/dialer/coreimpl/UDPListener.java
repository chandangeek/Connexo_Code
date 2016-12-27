package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.core.ListenerException;
import com.energyict.dialer.core.StreamConnection;
import com.energyict.dialer.core.UDPListen;

import java.io.IOException;

public class UDPListener extends ListenerImpl implements UDPListen {

    private ServerDatagramConnection sdc = null;

    public StreamConnection getAcceptedStreamConnection() {
        return sdc.getAcceptedDatagramConnection();
    }

    protected void doAccept(int timeout, int nrOfRings) throws NestedIOException, ListenerException {
        try {
            if (getStreamConnection() == null) {
                sdc = new ServerDatagramConnection(connectionString);
                setStreamConnection(sdc);
                getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
                getStreamConnection().serverOpen();
            }
            getStreamConnection().accept(timeout);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
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
