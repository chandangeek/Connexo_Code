/*
 * ATListener.java
 *
 * Created on 27 mei 2005, 15:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.LinkException;
import com.energyict.mdc.protocol.api.dialer.core.ListenerInitException;
import com.energyict.mdc.protocol.api.dialer.core.ListenerTimeoutException;
import com.energyict.mdc.protocol.api.dialer.core.StreamConnection;

import java.io.IOException;

/**
 * @author Koen
 */
public class ATListener extends ListenerImpl {

    protected ModemConnection modemConnection;
    int nrOfRings;

    protected void finalize() throws IOException, LinkException {
        disConnect();
    }

    public StreamConnection getAcceptedStreamConnection() {
        return getStreamConnection();
    }

    /**
     * Creates a new instance of ATListener
     */
    public ATListener() {
        modemConnection = new ModemConnection(this);
    }

    protected void doAccept(int iTimeout, int nrOfRings) throws IOException, LinkException {
        this.nrOfRings = nrOfRings;
        if (getSerialCommunicationChannel().sigCD()) {
            modemConnection.hangupModem();
        } else {
            modemConnection.toggleDTR(1000);
        }

        if (!modemConnection.isBoolAbort()) {
            try {
                modemConnection.initModem();
            } catch (LinkException e) {
                throw new ListenerInitException();
            }
        }
        if (!modemConnection.isBoolAbort()) {
            waitForCall(iTimeout);
        }
    }

    private void waitForCall(int timeout) throws IOException, LinkException {
        int ringCount = 0;
        boolean accepted = false;
        while (!accepted) {
            if (modemConnection.expectCommPort("RING")) {
                if (ringCount++ < (nrOfRings - 1)) {
                    continue;
                }

                accepted = true;
                write("ATA\r\n", 500);

                if (modemConnection.expectCommPort("CONNECT", timeout) == false) {
                    throw new ListenerTimeoutException("Timeout waiting for CONNECT with incoming call");
                } else {
                    try {
                        Thread.sleep(500);
                        getStreamConnection().flushInputStream(500);
                    } catch (InterruptedException e) {
                        throw new NestedIOException(e);
                    }
                }
            }
        }
    }

    protected void doConnect() throws IOException, LinkException {
    }

    protected void doDisConnect() throws IOException, LinkException {
        modemConnection.hangupModem();
    }

    protected void doDisConnectServer() throws IOException, LinkException {
        doDisConnect();
    }


}
