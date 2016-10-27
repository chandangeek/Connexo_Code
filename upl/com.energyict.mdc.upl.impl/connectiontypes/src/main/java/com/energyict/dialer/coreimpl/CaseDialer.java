package com.energyict.dialer.coreimpl;

import com.energyict.dialer.core.DialerException;
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
public class CaseDialer extends DialerImpl implements Modem {

    final int DELAY_AFTER_CONNECT_AND_FLUSH = getIntProperty("delayAfterConnectAndFlush", 500);

    protected CaseModemConnection caseModemConnection;

    protected void finalize() throws IOException, LinkException {
        disConnect();
    }

    /**
     * Class contstructor.
     */
    public CaseDialer() {
        caseModemConnection = new CaseModemConnection(this);
    }

    protected void doConnect(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException {
        if ((strDialAddress1 == null) || ("".compareTo(strDialAddress1) == 0)) {
            throw new DialerException("CaseDialer, strDialAddress (phoneNR) cannot be empty, correct first!");
        }
        if (iTimeout <= 0) {
            throw new DialerException("CaseDialer, invalid timeout value (=" + iTimeout + "), correct first!");
        }
        if (getSerialCommunicationChannel().sigCD()) {
            caseModemConnection.hangupModem();
        } else {
            caseModemConnection.toggleDTR(1000);
        }

        if (!caseModemConnection.isBoolAbort()) {
            caseModemConnection.initModem();
        }
        if (!caseModemConnection.isBoolAbort()) {
            caseModemConnection.dialModem(strDialPrefix, strDialAddress1, strDialAddress2, iTimeout, DELAY_AFTER_CONNECT_AND_FLUSH);
        }
    }

    protected void doDisConnect() throws IOException, LinkException {
        caseModemConnection.hangupModem();
    }

} // public class CaseDialer