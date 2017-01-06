/*
 * LinkImpl.java
 *
 * Created on 30 mei 2005, 13:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.coreimpl;

import com.energyict.cpo.Environment;
import com.energyict.dialer.core.DialerMarker;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.ListenerMarker;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.core.StreamConnection;
import com.energyict.protocol.tools.InputStreamObserver;
import com.energyict.protocol.tools.OutputStreamObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * @author Koen
 */
public abstract class LinkImpl implements Link {

    private static final Log logger = LogFactory.getLog(LinkImpl.class);

    public abstract void disConnect() throws IOException, LinkException; // KV 03102005

    public abstract void connect() throws IOException, LinkException; // KV 03102005

    String connectionString = null;
    protected String strModemInitCommPort = null;
    protected String strModemInitExtra = null;
    protected String strDialPrefix = null;

    StreamConnection streamConnection = null;

    InputStreamObserver inputStreamObserver = null;
    OutputStreamObserver outputStreamObserver = null;

    /**
     * Creates a new instance of LinkImpl
     */
    public LinkImpl() {
    }

    public void setStreamObservers(Object obj) {
        inputStreamObserver = (InputStreamObserver) obj;
        outputStreamObserver = (OutputStreamObserver) obj;
        // KV 03102005
        if (getStreamConnection() != null) {
            getStreamConnection().setStreamObservers(inputStreamObserver, outputStreamObserver);
        }
    }

    public java.io.InputStream getInputStream() {
        return getStreamConnection().getInputStream();
    }

    public java.io.OutputStream getOutputStream() {
        return getStreamConnection().getOutputStream();
    }

    public SerialCommunicationChannel getSerialCommunicationChannel() {
        return streamConnection;
    }

    public HalfDuplexController getHalfDuplexController() {
        return streamConnection;
    }

    public StreamConnection getStreamConnection() {
        return streamConnection;
    }

    public void setStreamConnection(StreamConnection streamConnection) {
        this.streamConnection = streamConnection;
    }

    //****************************************************************************************
    // Common core protected methods
    //****************************************************************************************

    protected void delay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void write(String strData, int iTimeout) throws IOException {
        getStreamConnection().write(strData, iTimeout);
    } // protected void writeCommPort(String strData, int iTimeout)

    protected void write(String strData) throws IOException {
        getStreamConnection().write(strData);
    } // protected void writeCommPort(String strData)


    //****************************************************************************************
    // Common core private methods
    //****************************************************************************************

    private boolean isTCP() {
        return (connectionString.indexOf("TCP") != -1);
    }

    private boolean isUDP() {
        return (connectionString.indexOf("UDP") != -1);
    }

    private boolean isLocalCOM() {
        if ((!isTCP()) && (!isUDP())) {
            StringTokenizer strTok = new StringTokenizer(connectionString, ":");
            if (strTok.countTokens() == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isIPtransparant() {
        if ((!isTCP()) && (!isUDP())) {
            if (!isLocalCOM()) {
                StringTokenizer strTok = new StringTokenizer(connectionString, ":");
                if (strTok.countTokens() == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isVirtualCOM() {
        if ((!isTCP()) && (!isUDP())) {
            if ((!isLocalCOM()) && (!isIPtransparant())) {
                StringTokenizer strTok = new StringTokenizer(connectionString, ":");
                if (strTok.countTokens() == 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createStreamConnection() throws LinkException {
        if (connectionString == null) {
            return;
        }

        if ((isTCP()) || (isUDP())) {
            streamConnection = null;
        } else if (isLocalCOM()) {
            streamConnection = new SerialPortStreamConnection(connectionString);
        } else if (isIPtransparant() || isVirtualCOM()) {
            streamConnection = new SocketStreamConnection(connectionString);
        } else {
            throw new LinkException("LinkImpl, createStreamConnection(), ERROR in connectionString " + connectionString);
        }
    }

    public void init(String connectionString) throws LinkException {
        init(connectionString, null, null, null);
    }

    public void init(String connectionString, String strModemInitCommPort) throws LinkException {
        init(connectionString, strModemInitCommPort, null, null);
    }

    public void init(String connectionString, String strModemInitCommPort, String strModemInitExtra) throws LinkException {
        init(connectionString, strModemInitCommPort, strModemInitExtra, null);
    }

    public void init(String connectionString, String strModemInitCommPort, String strModemInitExtra, String strDialPrefix) throws LinkException {
        this.connectionString = connectionString;
        this.strModemInitCommPort = strModemInitCommPort;
        this.strModemInitExtra = strModemInitExtra;
        this.strDialPrefix = strDialPrefix;
        if (((isTCP()) && (!DialerMarker.hasIPDialMarker(this))) && ((isTCP()) && (!ListenerMarker.hasIPListenMarker(this)))) {
            throw new LinkException("LinkImpl, TCP stream must be combined with an IPDialer or IPListener!");
        }
        if ((isUDP()) && (!ListenerMarker.hasUDPListenMarker(this))) {
            throw new LinkException("LinkImpl, UDP stream must be combined with an UDPListener!");
        }
        createStreamConnection();
    }

    protected int getIntProperty(String key, int defaultValue) {
        String value = Environment.getDefault().getProperty(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            // silently ignore
            return defaultValue;
        }
    }

    public String getStrModemInitCommPort() {
        return strModemInitCommPort;
    }

    public String getStrModemInitExtra() {
        return strModemInitExtra;
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
