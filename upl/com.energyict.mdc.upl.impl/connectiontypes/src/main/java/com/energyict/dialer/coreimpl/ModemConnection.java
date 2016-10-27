/*
 * ModemConnection.java
 *
 * Created on 30 mei 2005, 11:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.coreimpl;

import com.energyict.dialer.core.DialerCarrierException;
import com.energyict.dialer.core.DialerException;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.StreamConnection;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;

/**
 * @author Koen
 */
public class ModemConnection {

    Link link;
    //protected String strModemInitCommPort=null;
    //protected String strModemInitExtra=null;
    private boolean boolAbort = false;

    private static final String NO_DIALTONE = "NO DIALTONE";
    private static final String BUSY = "BUSY";
    private static final String NO_CARRIER = "NO CARRIER";
    private static final String ERROR = "ERROR";
    private static final String NO_ANSWER = "NO ANSWER";

    /**
     * Creates a new instance of ModemConnection
     */
    public ModemConnection(Link link) {
        this.link = link;
        boolAbort = false;
    }

    private StreamConnection getStreamConnection() {
        return link.getStreamConnection();
    }


    private void sendInitString(String strInit) throws IOException, LinkException {
        if ((strInit != null) && (strInit.trim().length() != 0)) {
            getStreamConnection().write(strInit + "\r\n", 1000);
            if (expectCommPort("OK", 5000) == false) {
                throw new LinkException("Modem initialization error for " + strInit);
            }
        }
    }

    protected void initModem() throws IOException, LinkException {
        int i;
        resetModem();
        for (i = 0; i < 3; i++) {
            getStreamConnection().write("ATZ\r\n", 500);
            if (expectCommPort("OK", 5000) == true) {
                break;
            }
        }
        if (i == 3) {
            throw new LinkException("Modem initialization error");
        }

        sendInitString(link.getStrModemInitExtra());
        sendInitString(link.getStrModemInitCommPort());

        String fixedInit = "ATS0=0E0V1\r\n";
        getStreamConnection().write(fixedInit, 500);
        if (expectCommPort("OK", 5000) == false) {
            throw new LinkException("Modem initialization error on " + fixedInit);
        }
    } // private void initModem() throws IOException

    // true  : expected string received
    // false : timeout
    protected boolean expectCommPort(String str) throws IOException, LinkException {
        return expectCommPort(str, -1);
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
            while (boolAbort == false) {
                if (getStreamConnection().getInputStream().available() != 0) {
                    inewKar = getStreamConnection().getInputStream().read();

                    strToParse += (char) inewKar;

                    if (strToParse.indexOf(str) != -1) {
                        return true;
                    } else if ((strToParse.indexOf(NO_DIALTONE) != -1) && (str.compareTo("OK") != 0)) {
                        throw new DialerException("NO DIALTONE received");
                    } else if ((strToParse.indexOf(BUSY) != -1) && (str.compareTo("OK") != 0)) {
                        throw new DialerException("BUSY received");
                    } else if ((strToParse.indexOf(ERROR) != -1) && (str.compareTo("OK") != 0)) {
                        throw new LinkException("ERROR received");
                    } else if ((strToParse.indexOf(NO_CARRIER) != -1) && (str.compareTo("OK") != 0)) {
                        throw new DialerCarrierException("NO CARRIER received");
                    } else if ((strToParse.indexOf(NO_ANSWER) != -1) && (str.compareTo("OK") != 0)) {
                        throw new DialerCarrierException("NO ANSWER received");
                    }
                } else {
                    Thread.sleep(100);
                }

                if (iTimeout != -1) {
                    if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                        return false;
                    }
                }
            } // while(boolAbort==false)

            return false;
        } catch (IOException e) {
            throw new LinkException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }


    } // private boolean Expect(String str,int iTimeout)  throws LinkException

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
    } // protected void hangupModem() throws LinkException

    protected void toggleDTR(int time) throws IOException {
        try {
            getStreamConnection().setDTR(false);
            Thread.sleep(time);
            getStreamConnection().setDTR(true);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    // KV 17042003
    // We use this method before initModem en for throws hangupModem. Virtual com ports require
    // this resetModem otherwise we get no response from virtual com port after a successfull ATDTx.
    // Virtual comm ports do not see the DTR drop!
    protected void resetModem() throws IOException, LinkException {
        getStreamConnection().write("+++", 500);
        expectCommPort("OK", 2000);
        getStreamConnection().write("ATH\r\n", 500);
        expectCommPort("OK", 2000);
        toggleDTR(1000);
    }

    public boolean isBoolAbort() {
        return boolAbort;
    }
}
