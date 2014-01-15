package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.LinkException;
import com.energyict.mdc.protocol.api.dialer.core.PostSelect;

import java.io.IOException;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class to set up a connection with a remote device via a modem. All calls are blocking by reading the inputstream.
 *         <BR>
 *         <B>Changes :</B><BR>
 *         KV 16042004 Initial version.<BR>
 * @version 1.0
 */
public class EMDialDialer extends ATDialer implements PostSelect {

    public EMDialDialer() {
    }

    // send id 3 times, so we are for 99% sure that it has been received...
    private void sendId(String strId) throws IOException {
        write(strId + "\r\n", 500);
        write(strId + "\r\n", 500);
        write(strId + "\r\n", 500);
    }

    protected void dialModem(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException {
//        String strId=null;
//        String strPhoneNR=strDialAddress1;
//
//        if (strDialAddress1.indexOf("$") != -1) {
//           strPhoneNR = strDialAddress1.substring(0,strDialAddress1.indexOf("$"));
//           strId = strDialAddress1.substring(strDialAddress1.indexOf("$"));
//        }

        if ((strDialPrefix != null) && (!strDialPrefix.trim().isEmpty())) {
            write("ATD" + strDialPrefix + strDialAddress1 + "\r\n", 500);
        } else {
            write("ATD" + strDialAddress1 + "\r\n", 500);
        }

        if (!modemConnection.expectCommPort("CONNECT", iTimeout)) {
            throw new DialerTimeoutException("Timeout waiting for CONNECT to phone " + strDialAddress1);
        } else {
            try {
                Thread.sleep(500);
                getStreamConnection().flushInputStream(500);

                if (strDialAddress2 != null) {
                    sendId(strDialAddress2);
                }

            } catch (InterruptedException | IOException e) {
                throw new NestedIOException(e);
            }
        }

    }

}