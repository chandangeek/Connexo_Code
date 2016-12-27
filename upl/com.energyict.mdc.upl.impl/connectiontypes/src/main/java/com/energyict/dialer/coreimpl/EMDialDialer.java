package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.core.DialerException;
import com.energyict.dialer.core.DialerTimeoutException;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.PostSelect;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

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

    /**
     * Class contstructor.
     *
     * @param strComPort : COMx port to use, e.g. EMDialDialer("COM1")
     * @throws DialerException
     */
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

        if ((strDialPrefix != null) && (strDialPrefix.trim().length() != 0)) {
            write("ATD" + strDialPrefix + strDialAddress1 + "\r\n", 500);
        } else {
            write("ATD" + strDialAddress1 + "\r\n", 500);
        }

        if (modemConnection.expectCommPort("CONNECT", iTimeout) == false) {
            throw new DialerTimeoutException("Timeout waiting for CONNECT to phone " + strDialAddress1);
        } else {
            try {
                Thread.sleep(500);
                getStreamConnection().flushInputStream(500);

                if (strDialAddress2 != null) {
                    sendId(strDialAddress2);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            } catch (IOException e) {
                throw new NestedIOException(e);
            }
        }

    } // private void dialModem(String strPhoneNR,int iTimeout) throws DialerException

    protected void hangupModem() throws IOException, LinkException {
        int i;
        if (getStreamConnection().isOpen()) {
            write("$0\r\n", 500);
            write("$0\r\n", 500);
            for (i = 0; i < 3; i++) {
                modemConnection.resetModem();
                try {
                    if (!getSerialCommunicationChannel().sigCD()) {
                        break;
                    }
                } catch (IOException e) {
                    throw new NestedIOException(e);
                }
            }
            if (i == 3) {
                throw new DialerException("Hangup modem failed");
            }
        } // if (boolOpen)
    } // protected void hangupModem() throws DialerException

} // public class EMDialDialer