/*
 * PAKNETDialer.java
 *
 * Created on 8 april 2004, 14:09
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.core.DialerException;
import com.energyict.dialer.core.DialerTimeoutException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;

// KV TO_DO implement some error handling with the paknet modem...

/**
 * @author Koen
 */
public class PAKNETDialer extends DialerImpl {

    boolean boolAbort = false;
    String lastResponse;

    /**
     * Creates a new instance of PAKNETDialer
     */
    public PAKNETDialer() {
        boolAbort = false;
    }

    protected void doConnect(String strDialAddress1, String strDialAddress2, int iTimeout) throws NestedIOException, DialerException {
        try {
            // All Paknet modems use parity for the service commands!
            getSerialCommunicationChannel().setParity(SerialCommunicationChannel.DATABITS_7, SerialCommunicationChannel.PARITY_EVEN, SerialCommunicationChannel.STOPBITS_1);
            //getSerialCommunicationChannel().setParams(9600,SerialCommunicationChannel.DATABITS_7,SerialCommunicationChannel.PARITY_EVEN,SerialCommunicationChannel.STOPBITS_1);
            waitForPrompt();
            getStreamConnection().flushInputStream(500);
            setParams();
            getStreamConnection().flushInputStream(500);

            // KV 020606
            if ((strDialPrefix != null) && (strDialPrefix.trim().length() != 0)) {
                waitForConnectionPrompt(strDialPrefix + strDialAddress1);
            } else {
                waitForConnectionPrompt(strDialAddress1);
            }

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
        throw new DialerException("PAKNETDialer, no '*' prompt received after " + MAX_RETRY_PROMPT + " attempts!" + validateLastResponse());
    }

    final int MAX_RETRY_PARAMS = getIntProperty("paknetMaxRetryParams", 5);
    final int TIMEOUT_PARAMS = getIntProperty("paknetTimeoutParams", 10); // in sec.

    private boolean setParams() throws IOException, DialerException {
        int retries = 0;
        do {
            write("SET 1:0,2:0,3:0,4:10,5:0,6:5\r");
            if (expect("*", TIMEOUT_PARAMS * 1000)) {
                return true;
            }
        } while (retries++ <= (MAX_RETRY_PARAMS - 1));
        throw new DialerTimeoutException("PAKNETDialer, no '*' prompt received after " + MAX_RETRY_PARAMS + " attempts to det parameters!" + validateLastResponse());
    }

    final int MAX_RETRY_CONNECTION_PROMPT = getIntProperty("paknetMaxRetryConnection", 5);
    final int TIMEOUT_CONNECTION_PROMPT = getIntProperty("paknetTimeoutConnection", 30); // in sec.

    private boolean waitForConnectionPrompt(String address) throws IOException, DialerException {


        lastResponse = null;
        int addressIndex = 0;
        do {
            write(address + "\r");
            if (expect("COM", TIMEOUT_CONNECTION_PROMPT * 1000)) {
                return true;
            }
        } while (addressIndex++ <= (MAX_RETRY_CONNECTION_PROMPT - 1));
        throw new DialerTimeoutException("PAKNETDialer, no 'COM' prompt received after " + MAX_RETRY_CONNECTION_PROMPT + " attempts!" + validateLastResponse());
    } // private boolean waitForPAKNETPrompt(String key)

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
        //boolean dataReceived=false;

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
//                    else if (strToParse.indexOf("\r\n") != -1) {
//                        strToParse = "";
//                        throw new DialerException("Error from PAKNET Radio PAD");
//                    }

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
}
