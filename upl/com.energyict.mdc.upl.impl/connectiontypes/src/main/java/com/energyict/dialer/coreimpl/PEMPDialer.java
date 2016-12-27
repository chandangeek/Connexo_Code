/*
 * PEMPDialer.java
 *
 * Created on 8 april 2004, 14:09
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.channels.serial.modem.PEMPModemConfiguration;
import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.core.DialerException;
import com.energyict.dialer.core.DialerTimeoutException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * @author Koen
 */
public class PEMPDialer extends DialerImpl {

    boolean boolAbort = false;
    String lastResponse;

    /**
     * Creates a new instance of PEMPDialer
     */
    public PEMPDialer() {
        boolAbort = false;
    }

    protected void doConnect(String strDialAddress1, String strDialAddress2, int iTimeout) throws NestedIOException, DialerException {
        try {
            // All Paknet modems use parity for the service commands!
            getSerialCommunicationChannel().setParity(SerialCommunicationChannel.DATABITS_7, SerialCommunicationChannel.PARITY_EVEN, SerialCommunicationChannel.STOPBITS_1);

            StringTokenizer strTok = new StringTokenizer(strDialAddress1, "_");
            String key = strTok.nextToken(); //strDialAddress1.split("_")[0];
            String address = strTok.nextToken(); //strDialAddress1.split("_")[1];
            waitForPrompt();
            getStreamConnection().flushInputStream(500);
            waitForPEMPPrompt(key);
            getStreamConnection().flushInputStream(500);
            waitForConnectionPrompt(key, address);
            getStreamConnection().flushInputStream(500);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    } // protected void doConnect(String strDialAddress1, String strDialAddress2, int iTimeout) throws NestedIOException, DialerException

    protected void doDisConnect() throws NestedIOException, DialerException {
        try {
            delay(200);
            getStreamConnection().setDTR(false);
            delay(DTR_TOGGLE_DELAY);
            getStreamConnection().setDTR(true);
            delay(DTR_TOGGLE_DELAY);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    //****************************************************************************************
    // Common core private methods
    //****************************************************************************************
    final int MAX_RETRY_PROMPT = getIntProperty("paknetMaxRetryPrompt", 5);
    final int TIMEOUT_PROMPT = getIntProperty("paknetTimeoutPrompt", 10); // in sec.
    final int DTR_TOGGLE_DELAY = getIntProperty("paknetDtrToggleDelay", 2000); // in msec

    /*
    *  Send CR and wait for *. Retry for MAX_RETRY_PROMPT times after a timeout of TIMEOUT_PROMPT sec
    */
    private boolean waitForPrompt() throws IOException, DialerException {
        lastResponse = null;
        int retries = 0;
        do {
            write("\r");
            if (expect("*", TIMEOUT_PROMPT * 1000)) {
                return true;
            }
        } while (retries++ <= (MAX_RETRY_PROMPT - 1));
        throw new DialerTimeoutException("PEMPDialer, no '*' prompt received after " + MAX_RETRY_PROMPT + " attempts!" + validateLastResponse());
    }

    final int TIMEOUT_FENS_CONNECTION_PROMPT = getIntProperty("pempFENSTimeoutConnection", 30); // in sec.

    private boolean waitForPEMPPrompt(String key) throws IOException, DialerException {
        lastResponse = null;
        int addressIndex = 0;
        PEMPModemConfiguration pempLookup = PEMPModemConfiguration.getPEMPModemConfiguration(key);
        do {
            write(pempLookup.getAddresses()[addressIndex] + "\r");
            delay(200);
            getStreamConnection().setDTR(false);
            getStreamConnection().setDTR(true);
            delay(1000);
            write(pempLookup.getAddresses()[addressIndex] + "\r");

            if (expect(pempLookup.getPromptResponse(), TIMEOUT_FENS_CONNECTION_PROMPT * 1000)) {
                return true;
            }
        } while (addressIndex++ <= (pempLookup.getAddresses().length - 1));
        throw new DialerTimeoutException("PEMPDialer, no '" + pempLookup.getPromptResponse() + "' prompt received after " + pempLookup.getAddresses().length + " attempts!" + validateLastResponse());
    } // private boolean waitForPEMPPrompt(String key)

    final int MAX_RETRY_CONNECTION_PROMPT = getIntProperty("paknetMaxRetryConnection", 5);
    final int TIMEOUT_COM_CONNECTION_PROMPT = getIntProperty("pempCOMTimeoutConnection", 30); // in sec.

    private boolean waitForConnectionPrompt(String key, String address) throws IOException, DialerException {
        lastResponse = null;
        int addressIndex = 0;
        PEMPModemConfiguration pempLookup = PEMPModemConfiguration.getPEMPModemConfiguration(key);
        do {
            write(address + "\r");
            if (expect(pempLookup.getConnectionResponse(), TIMEOUT_COM_CONNECTION_PROMPT * 1000)) {
                return true;
            }
        } while (addressIndex++ <= (MAX_RETRY_CONNECTION_PROMPT - 1));
        throw new DialerTimeoutException("PEMPDialer, no '" + pempLookup.getConnectionResponse() + "' prompt received after " + MAX_RETRY_CONNECTION_PROMPT + " attempts!" + validateLastResponse());
    } // private boolean waitForPEMPPrompt(String key)

    private String validateLastResponse() {
        if ((lastResponse != null) && ("".compareTo(lastResponse) != 0)) {
            return " (last response from PAKNET Radio PAD=" + lastResponse + ", Consult the PAKNET Radio-Pad technical manual for more information!)";
        }
        return "";
    }

    // true  : expected string received
    // false : timeout
    private boolean expect(String str, int iTimeout) throws NestedIOException, DialerException {
        long lMSTimeout;
        String strToParse;
        int inewKar;

        strToParse = "";
        lMSTimeout = System.currentTimeMillis() + iTimeout;
        try {
            while (boolAbort == false) {
                if (getStreamConnection().getInputStream().available() != 0) {
                    inewKar = getStreamConnection().getInputStream().read();

                    strToParse += (char) inewKar;

                    if (strToParse.indexOf(str) != -1) {
                        strToParse = "";
                        return true;
                    }
                } else {
                    Thread.sleep(100);
                }

                if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                    lastResponse = strToParse;
                    return false;
                }
            } // while(boolAbort==false)

            //            boolAbort=false;  // not yet...
            return false;
        } catch (IOException e) {
            throw new DialerException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    } // private boolean Expect(String str,int iTimeout)  throws DialerException
} // public class PEMPDialer extends DialerImpl
