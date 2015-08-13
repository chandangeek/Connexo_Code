package com.elster.protocolimpl.lis200;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Connection class that allows a wake up before the IEC1107 Sign on
 *
 * @author gna
 * @since 23-feb-2010
 *        <p/>
 *        copied from DL220Connection by gh
 */
@SuppressWarnings({"unused"})
public class Lis200Connection extends FlagIEC1107Connection {

    /**
     * property to remember identStr for later evaluation
     */
    private String identifier = "";
    private boolean disableAutoLogoff;
    private InputStream inputStream;

    private boolean suppressWakeupSequence;
    private int delayAfterCheck;
    /**
     * Default constructor for the DL220 protocol
     *
     * @param inputStream            - the {@link InputStream} to use
     * @param outputStream           - the {@link OutputStream} to use
     * @param timeout                - Time in ms. for a request to wait for a response before
     *                               returning an timeout error.
     * @param maxRetries             - nr of retries before fail in case of a timeout or
     *                               recoverable failure
     * @param forceDelay             - delay before send. Some protocols have troubles with fast
     *                               send/receive
     * @param echoCancelling         - echo cancelling on/off
     * @param compatible             - behave full compatible or use protocol special features
     * @param software7e1            - property to dial with a GSM modem using 7 databits, even
     *                               parity, 1 stopbit
     * @param suppressWakeupSequence - if wakeup sequence should not be send
     * @throws ConnectionException - in case of error
     */
    public Lis200Connection(InputStream inputStream, OutputStream outputStream,
                            int timeout, int maxRetries, int forceDelay, int echoCancelling,
                            int compatible, boolean software7e1, boolean suppressWakeupSequence,
                            boolean disableAutoLogoff, int delayAfterCheck)
            throws ConnectionException {
        super(inputStream, outputStream, timeout, maxRetries, forceDelay,
                echoCancelling, compatible, software7e1);
        this.inputStream = inputStream;
        this.suppressWakeupSequence = suppressWakeupSequence;
        this.disableAutoLogoff = disableAutoLogoff;
        this.delayAfterCheck = delayAfterCheck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeterType connectMAC(String strIdentConfig, String strPass,
                                int iSecurityLevel, String meterID, int baudRate)
            throws IOException {

        this.strIdentConfig = strIdentConfig;
        this.strPass = strPass;
        this.iSecurityLevel = iSecurityLevel;
        this.meterID = meterID;
        this.baudrate = baudRate;

        if (!boolFlagIEC1107Connected) {
            MeterType meterType = null;

            boolean stillConnected = false;
            try {
                // KV 18092003
                if (hhuSignOn == null) {

                    if (disableAutoLogoff) {
                        meterType = tryReceiveDeviceType();
                        if (delayAfterCheck > 0) {
                            try {
                                Thread.sleep(delayAfterCheck);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
                            }
                        }
                    }

                    stillConnected = meterType != null;

                    if (!stillConnected) {
                        /* add wakeup sequence! */
                        if (!suppressWakeupSequence) {
                            sendWakeupSequence();
                        }
                        meterType = signOn(strIdentConfig, meterID);
                    }
                } else {
                    meterType = hhuSignOn.signOn(strIdentConfig, meterID, true, baudRate);
                }
                boolFlagIEC1107Connected = true;

                if (!stillConnected) {
                    prepareAuthentication(strPass);
                }
                sessionState = STATE_PROGRAMMINGMODE;
                identifier = meterType.getReceivedIdent();
                return meterType;
            } catch (FlagIEC1107ConnectionException e) {
                throw new FlagIEC1107ConnectionException(
                        "connectMAC(), FlagIEC1107ConnectionException "
                                + e.getMessage());
            } catch (ConnectionException e) {
                throw new FlagIEC1107ConnectionException(
                        "connectMAC(), ConnectionException " + e.getMessage());
            }
        } // if (boolFlagIEC1107Connected==false

        return null;

    }

    private MeterType tryReceiveDeviceType() {

        try {
            sendRawData(new byte[]{0x01, 0x52, 0x31, 0x02, 0x30, 0x31, 0x3a, 0x30, 0x31, 0x38, 0x31,
                    0x2e, 0x30, 0x28, 0x31, 0x29, 0x03, 0x7F});

            boolean quitLoop = false;
            String answer = "";

            long timeoutMillis = System.currentTimeMillis() + 5 * 1000;

            while (System.currentTimeMillis() < timeoutMillis) {
                if (inputStream.available() > 0) {
                    int byteValue = inputStream.read();

                    // loop breaks AFTER reading checksum (follows 0x03)
                    if (quitLoop) {
                        break;
                    }
                    quitLoop = byteValue == 0x03;

                    if (byteValue >= 0x20) {
                        answer = answer + (char) byteValue;
                    }
                }
            }

            if (quitLoop && (answer.length() > 0)) {
                int p1 = answer.indexOf("(");
                int p2 = answer.indexOf(")");
                String d = answer.substring(p1 + 1, p2);
                MeterType meterType = new MeterType("/Els6" + d);
                return meterType;
            }
            return null;

        } catch (IOException ignore) {
            return null;
        }
    }

    /**
     * Send a sequence of 0x00 to the end device (wakeup sequence for elster
     * lis200 devices)
     *
     * @throws IOException - in case of error
     */
    private void sendWakeupSequence() throws IOException {
        /* send 0 during 2 seconds */
        long endTime = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < endTime) {
            sendRawData(new byte[]{0});
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
            }
        }
    }

    public String getReadIdentifier() {
        return identifier;
    }

}