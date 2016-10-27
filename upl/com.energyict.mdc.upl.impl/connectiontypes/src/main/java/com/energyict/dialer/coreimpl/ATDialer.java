package com.energyict.dialer.coreimpl;

import com.energyict.dialer.core.DialerException;
import com.energyict.dialer.core.DialerTimeoutException;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.Modem;

import java.io.IOException;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class to set up a connection with a remote device via a modem. All calls are blocking by reading the inputstream.
 *         <BR>
 *         <B>Changes :</B><BR>
 *         KV 24042002 Initial version.<BR>
 *         KV 26062002 Add constructor with extra initstring for modem, e.g. to activate V110 mode.<BR>
 *         KV 29012004 Changed code to use serialio's driver cause Sun's javax.comm api has a handle leak bug
 *         Remark: jspwin.dll from serialio has been changed on 30/01/2004 due to a bug.
 *         DTR & RTS behaviour is also changed in this new dll. DTR & RTS set to high on serialport open and low on serialport close.
 *         KV 12022004 Extended to use socket communication.
 *         KV 17032004 Add HalfDuplex support
 * @version 1.0
 */
public class ATDialer extends DialerImpl implements Modem {

    protected ModemConnection modemConnection;

    protected void finalize() throws IOException, LinkException {
        disConnect();
    }

    /**
     * Class contstructor.
     */
    public ATDialer() {
        modemConnection = new ModemConnection(this);
    }

    final int DELAY_AFTER_CONNECT_AND_FLUSH = getIntProperty("delayAfterConnectAndFlush", 500);

    protected void dialModem(String strPhoneNR, String selector, int iTimeout) throws IOException, LinkException {
        if ((strDialPrefix != null) && (strDialPrefix.trim().length() != 0)) {
            write("ATD" + strDialPrefix + strPhoneNR + "\r\n", 500);
        } else {
            write("ATD" + strPhoneNR + "\r\n", 500);
        }

        if (modemConnection.expectCommPort("CONNECT", iTimeout) == false) {
            throw new DialerTimeoutException("Timeout waiting for CONNECT to phone " + strPhoneNR);
        } else {
            getStreamConnection().flushInputStream(DELAY_AFTER_CONNECT_AND_FLUSH);
            if ((selector != null) && (selector.trim().length() != 0)) {
                write(selector, 500);
            }
        }

    } // private void dialModem(String strPhoneNR,int iTimeout) throws DialerException


    protected void doConnect(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException {
        if ((strDialAddress1 == null) || ("".compareTo(strDialAddress1) == 0)) {
            throw new DialerException("ATDialer, strDialAddress (phoneNR) cannot be empty, correct first!");
        }
        if (iTimeout <= 0) {
            throw new DialerException("ATDialer, invalid timeout value (=" + iTimeout + "), correct first!");
        }
        if (getSerialCommunicationChannel().sigCD()) {
            modemConnection.hangupModem();
        } else {
            modemConnection.toggleDTR(1000);
        }

        if (!modemConnection.isBoolAbort()) {
            modemConnection.initModem();
        }
        if (!modemConnection.isBoolAbort()) {
            dialModem(strDialAddress1, strDialAddress2, iTimeout);
        }
    }

    protected void doDisConnect() throws IOException, LinkException {
        modemConnection.hangupModem();
    }

} // public class ATDialer