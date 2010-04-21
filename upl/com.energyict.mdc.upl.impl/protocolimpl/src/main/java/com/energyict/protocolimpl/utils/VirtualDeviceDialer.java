package com.energyict.protocolimpl.utils;

import com.energyict.dialer.core.*;
import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpFile;

import java.io.*;

/**
 * Copyrights EnergyICT
 * Date: 21-apr-2010
 * Time: 13:19:39
 */
public class VirtualDeviceDialer implements Dialer {

    private final VirtualDevice virtualDevice;
    private StreamConnection serialCommunicationChannel;

    public VirtualDeviceDialer(String communicationDumpFile) {
        this(new CommunicationDumpFile(communicationDumpFile));
    }

    public VirtualDeviceDialer(CommunicationDumpFile communicationDumpFile) {
        this(new VirtualDevice(communicationDumpFile));
    }

    public VirtualDeviceDialer(VirtualDevice virtualDevice) {
        this.virtualDevice = virtualDevice;
    }

    public void setShowCommunication(boolean showCommunication) {
        virtualDevice.setShowCommunication(showCommunication);
    }

    public void connect() throws IOException, LinkException {
        virtualDevice.reset();
    }

    public void connect(String strDialAddress1, int iTimeout) throws IOException, LinkException {
        connect();
    }

    public void connect(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException {
        connect();
    }

    public InputStream getInputStream() {
        return virtualDevice.getInputStream();
    }

    public OutputStream getOutputStream() {
        return virtualDevice.getOutputStream();
    }

    public SerialCommunicationChannel getSerialCommunicationChannel() {
        if (serialCommunicationChannel == null) {
            serialCommunicationChannel = new VirtualSerialCommunicationChannel(this);
        }
        return serialCommunicationChannel;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public HalfDuplexController getHalfDuplexController() {
        return getSerialCommunicationChannel();
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
        init(connectionString, "");
    }

    public void init(String connectionString, String strModemInitCommPort) throws LinkException {
        init(connectionString, strModemInitCommPort, "");
    }

    public void init(String connectionString, String strModemInitCommPort, String strModemInitExtra) throws LinkException {
        init(connectionString, strModemInitCommPort, strModemInitExtra, "");
    }

    public void init(String connectionString, String strModemInitCommPort, String strModemInitExtra, String strDialPrefix) throws LinkException {

    }

    public String getStrModemInitCommPort() {
        return null;
    }

    public String getStrModemInitExtra() {
        return null;
    }

    public void disConnect() throws IOException, LinkException {
        virtualDevice.reset();
    }

    public void connectDialer(Dialer dialer) {
        connectDialer(dialer,  "");
    }

    public void connectDialer(Dialer dialer, String dialAddr1) {
        connectDialer(dialer, dialAddr1, "");
    }

    public void connectDialer(Dialer dialer, String dialAddr1, String dialAddr2) {
        connectDialer(dialer, dialAddr1, dialAddr2, 10000);
    }

    public void connectDialer(Dialer dialer, String dialAddr1, String dialAddr2, int timeout) {

    }
}
