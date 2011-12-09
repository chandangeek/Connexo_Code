package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.dialer.core.*;

import java.io.*;

/**
 * Copyrights EnergyICT
 * Date: 25-jan-2011
 * Time: 13:17:00
 */
public class DummyLink implements Link {

    public InputStream getInputStream() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public OutputStream getOutputStream() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SerialCommunicationChannel getSerialCommunicationChannel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public HalfDuplexController getHalfDuplexController() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public StreamConnection getStreamConnection() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStreamConnection(StreamConnection streamConnection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStreamObservers(Object obj) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void init(String connectionString) throws LinkException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void init(String connectionString, String strModemInitCommPort) throws LinkException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void init(String connectionString, String strModemInitCommPort, String strModemInitExtra) throws LinkException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void init(String connectionString, String strModemInitCommPort, String strModemInitExtra, String strDialPrefix) throws LinkException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getStrModemInitCommPort() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getStrModemInitExtra() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disConnect() throws IOException, LinkException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void connect() throws IOException, LinkException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Update the StreamConnection so proper monitoring can be performed by the ComServer
     *
     * @param streamConnection the <i>new</i> StreamConnection
     */
    public void updateStreamConnection(StreamConnection streamConnection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
