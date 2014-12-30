/*
 * ModemConnection.java
 *
 * Created on 30 mei 2005, 11:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.DialerException;
import com.energyict.mdc.protocol.api.dialer.core.Link;
import com.energyict.mdc.protocol.api.dialer.core.LinkException;
import com.energyict.mdc.protocol.api.dialer.core.StreamConnection;

import java.io.IOException;

/**
 * @author Koen
 */
public class CaseModemConnection {

    Link link;
    private boolean boolAbort = false;

    // initialisatie
    private static final String ECHO_OFF_COMMAND = "L0";
    private static final String ECHO_OFF = "ECHO OFF";
    private static final String DTR_NORMAL_COMMAND = "O0";
    private static final String DTR_NORMAL = "DTR NORMAL";
    private static final String ERROR_CORRECTING_MODE_COMMAND = "V0";
    private static final String ERROR_CORRECTING_MODE = "ERROR CORRECTING MODE";


    // after D[phonenr]
    private static final String DIALLING = "DIALLING";
    // after a while...
    private static final String AWAITING_CARRIER = "AWAITING CARRIER";

    // in case the modem on the other answers
    private static final String LINK_ESTABLISHED = "LINK ESTABLISHED";


    // in case the modem on the other site doesn't answer
    private static final String CALL_ABORTED = "CALL ABORTED";

    // after DTR dropped, hangup call initiation
    private static final String CALL_CLEARED_DOWN = "CALL CLEARED DOWN";


    /**
     * Creates a new instance of ModemConnection
     */
    public CaseModemConnection(Link link) {
        this.link = link;
        boolAbort = false;
    }

    private StreamConnection getStreamConnection() {
        return link.getStreamConnection();
    }

    protected void dialModem(String strDialPrefix, String strPhoneNR, String selector, int iTimeout, long delayAfterconnectAndFlush) throws IOException, LinkException {
        if ((strDialPrefix != null) && (!strDialPrefix.trim().isEmpty())) {
            getStreamConnection().write("D" + strDialPrefix + strPhoneNR + "\r\n", 500);
        } else {
            getStreamConnection().write("D" + strPhoneNR + "\r\n", 500);
        }

        if (!expectCommPort(LINK_ESTABLISHED, iTimeout)) {
            throw new DialerTimeoutException("Timeout waiting for CONNECT to phone " + strPhoneNR);
        } else {
            getStreamConnection().flushInputStream(delayAfterconnectAndFlush);
            if ((selector != null) && (!selector.trim().isEmpty())) {
                getStreamConnection().write(selector, 500);
            }
        }

    } // private void dialModem(String strPhoneNR,int iTimeout) throws DialerException

    private void sendInitString(String strInit, String expecting) throws IOException, LinkException {
        if ((strInit != null) && (!strInit.trim().isEmpty()) && (expecting != null) && (!expecting.trim().isEmpty())) {
            int i;
            for (i = 0; i < 3; i++) {
                getStreamConnection().write(strInit + "\r\n", 1000);
                if (expectCommPort(expecting, 5000)) {
                    break;
                }
            }
            if (i == 3) {
                throw new LinkException("Modem initialization error for " + strInit + " expecting " + expecting);
            }
        }
    }

    protected void initModem() throws IOException, LinkException {
        resetModem();
        sendInitString(ECHO_OFF_COMMAND, ECHO_OFF);
        sendInitString(DTR_NORMAL_COMMAND, DTR_NORMAL);
        sendInitString(ERROR_CORRECTING_MODE_COMMAND, ERROR_CORRECTING_MODE);
        sendInitString(link.getStrModemInitExtra(), "\r\n");
        sendInitString(link.getStrModemInitCommPort(), "\r\n");
    }

    protected boolean expectCommPort(String str, int iTimeout) throws IOException, LinkException {
        long lMSTimeout = -1;
        String strToParse;
        int inewKar;

        strToParse = "";
        if (iTimeout != -1) {
            lMSTimeout = System.currentTimeMillis() + iTimeout;
        }
        try {
            while (!boolAbort) {
                if (getStreamConnection().getInputStream().available() != 0) {
                    inewKar = getStreamConnection().getInputStream().read();

                    strToParse += (char) inewKar;

                    if (strToParse.contains(str)) {
                        return true;
                    } else if (strToParse.contains(CALL_ABORTED)) {
                        throw new DialerException("CALL ABORTED received");
                    }
                } else {
                    Thread.sleep(100);
                }

                if (iTimeout != -1) {
                    if (System.currentTimeMillis() - lMSTimeout > 0) {
                        return false;
                    }
                }
            }
            return false;
        } catch (IOException e) {
            throw new LinkException(e);
        } catch (InterruptedException e) {
            throw new NestedIOException(e);
        }
    }

    protected void hangupModem() throws IOException, LinkException {
        int i;
        if (getStreamConnection().isOpen()) {
            for (i = 0; i < 3; i++) {
                resetModem();
                if (!getStreamConnection().sigCD()) {
                    break;
                }
            }
            if (i == 3) {
                throw new LinkException("Hangup modem failed");
            }
        }
    }

    protected void toggleDTR(int time) throws IOException {
        try {
            getStreamConnection().setDTR(false);
            Thread.sleep(time);
            getStreamConnection().setDTR(true);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new NestedIOException(e);
        }
    }

    // KV 17042003
    // We use this method before initModem en for throws hangupModem. Virtual com ports require
    // this resetModem otherwise we get no response from virtual com port after a successfull ATDTx.
    // Virtual comm ports do not see the DTR drop!
    private void resetModem() throws IOException {
        toggleDTR(1000);
    }

    public boolean isBoolAbort() {
        return boolAbort;
    }
}
