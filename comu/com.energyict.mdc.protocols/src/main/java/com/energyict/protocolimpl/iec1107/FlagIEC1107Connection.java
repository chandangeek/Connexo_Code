/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.protocols.mdc.inbound.general.MeterTypeImpl;

import com.energyict.dialer.connection.Connection;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.Encryptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koenraad Vanderschaeve
 *         <p/>
 *         <B>Description :</B><BR>
 *         Class that implements the Flag IEC1107 datalink layer protocol.
 *         <B>Changes :</B><BR>
 *         KV 04102002 Initial version.<BR>
 *         KV 06102003 changed delay after break 300ms -> DELAY_AFTER_BREAK macro 2000ms
 *         bugfix in method doDataReadout(), set Z (baudrate) parameter
 *         KV 06072004 Add delayAndFlush(2000) to disconnect
 *         Check for password != null if security level higher then 0 is requested
 *         Add security level cause...
 *         KV 27102004 Make more robust against marginal communication quality
 *         KV 21122004 Clarify exception messages and add state to control NAK
 *         JM 22012009 Added method to disconnect meter without sending a break command to
 *         prevent communication timeouts or non responding devices with
 *         Elster meters (ABBA1350, ABBA1500, ...)
 *         GN 01042009 Generalized the Software7E1 from the Kamstrup for all FlagIEC1107 protocols
 * @version 1.0
 */
public class FlagIEC1107Connection extends Connection {

    private static final byte DEBUG = 0;
    private static final int DELAY_AFTER_BREAK = 2000; // KV 06102003

    private ByteArrayOutputStream echoByteArrayOutputStream = new ByteArrayOutputStream();
    //private ByteArrayInputStream echoByteArrayInputStream;

    private static final int TIMEOUT = 600000;

    public static final byte UNKNOWN_ERROR = -1;
    public static final byte TIMEOUT_ERROR = -2;
    public static final byte SECURITYLEVEL_ERROR = -3; // KV 06072004

     public static final byte RECONNECT_ERROR = -9; // KV 17012008

    private byte[] authenticationCommand = null;
    private byte[] authenticationData = null;

    private int iMaxRetries;

    protected Logger logger;

    // General attributes
    private int iProtocolTimeout;
    private int iIEC1107Compatible;
    protected int iSecurityLevel;

    protected static final byte SOH = 0x01;
    protected static final byte STX = 0x02;
    protected static final byte ETX = 0x03;
    private static final byte EOT = 0x04;
    private static final byte ACK = 0x06;
    private static final byte NAK = 0x15;

    // specific IEC1107
    protected boolean boolFlagIEC1107Connected;

    private long lForceDelay;
    private int iEchoCancelling;

    private Encryptor encryptor;

    private byte[] txBuffer = null, txBuffer2 = null;


    protected static final int STATE_SIGNON = 0;
    protected static final int STATE_PROGRAMMINGMODE = 1;

    protected int sessionState = STATE_SIGNON;
    private String errorSignature = "ERR";

    private boolean addCRLF = false;
    private boolean software7E1;

    protected String strIdentConfig;
    protected String strPass;
    protected String meterID;
    protected int baudrate;
    int connectCount = 0;

    private boolean dontSendLogOffCommand = true;

    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 boolean software7E1) throws ConnectionException {
        this(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, software7E1, null);
    }

    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 boolean software7E1,
                                 Logger logger) throws ConnectionException {
        this(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, null, software7E1, logger);
    }

    /**
     * Class constructor.
     *
     * @param inputStream        InputStream for the active connection
     * @param outputStream       OutputStream for the active connection
     * @param iTimeout           Time in ms. for a request to wait for a response before returning an timeout error.
     * @param iMaxRetries        nr of retries before fail in case of a timeout or recoverable failure
     * @param lForceDelay        delay before send. Some protocols have troubles with fast send/receive
     * @param iEchoCancelling    echo cancelling on/off
     * @param iIEC1107Compatible behave full compatible or use protocol special features
     * @param encryptor          if the protocol has an encryptor to use for security level 2 password logon
     * @throws FlagIEC1107ConnectionException
     */
    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor,
                                 boolean software7E1) throws ConnectionException {
        this(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, encryptor, software7E1, null);
    }

    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor,
                                 boolean software7E1,
                                 Logger logger) throws ConnectionException {
        this(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, encryptor, null, software7E1, logger);
    }

    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor,
                                 HalfDuplexController halfDuplexController,
                                 boolean software7E1) throws ConnectionException {
        this(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, encryptor, halfDuplexController, software7E1, null);
    }

    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor,
                                 HalfDuplexController halfDuplexController,
                                 boolean software7E1,
                                 Logger logger) throws ConnectionException {
        super((software7E1 ? new Software7E1InputStream(inputStream) : inputStream),
                (software7E1 ? new Software7E1OutputStream(outputStream) : outputStream), lForceDelay, iEchoCancelling, halfDuplexController);
        this.iMaxRetries = iMaxRetries;
        this.lForceDelay = lForceDelay;
        this.iEchoCancelling = iEchoCancelling;
        this.iIEC1107Compatible = iIEC1107Compatible;
        this.encryptor = encryptor;
        this.software7E1 = software7E1;
        iProtocolTimeout = iTimeout;
        boolFlagIEC1107Connected = false;
        this.logger = logger;
    } // public FlagIEC1107Connection(...)

    /**
     * Constructor with an additional argument
     *
     * @param inputStream
     *          The used InputStream
     *
     * @param outputStream
     *          The used OutputStream
     *
     * @param iTimeout
     *          The timeout before throwing a ConnectionException
     *
     * @param iMaxRetries
     *          The number of retry attempts
     *
     * @param lForceDelay
     *          The delay before sending a new command
     *
     * @param iEchoCancelling
     *          Indicate whether to ignore the echo from the modem
     *
     * @param iIEC1107Compatible
     *          Indicate whether it is a fully compatible IEC1107 protocol to use specific read commands
     *
     * @param encryptor
     *          The used Encryptor
     *
     * @param halfDuplexController
     *          Indication whether to use halfduplex
     *
     * @param software7E1
     *          Indicate whether a GSM modem is used to reformat the data structure
     *
     * @param dontSendBreakCommand
     *          Indicate whether it is allowed to send a break command before a retry (some modems see this as a logoff)
     *
     * @throws ConnectionException
     *          The result of unexpected behavior of the connection
     */
    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor,
                                 HalfDuplexController halfDuplexController,
                                 boolean software7E1,
                                 boolean dontSendBreakCommand) throws ConnectionException {
        this(inputStream, outputStream, iTimeout, iMaxRetries,lForceDelay,iEchoCancelling,iIEC1107Compatible,encryptor, halfDuplexController, software7E1, dontSendBreakCommand, null);
    }

    public FlagIEC1107Connection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor,
                                 HalfDuplexController halfDuplexController,
                                 boolean software7E1,
                                 boolean dontSendBreakCommand,
                                 Logger logger) throws ConnectionException {
        this(inputStream, outputStream, iTimeout, iMaxRetries,lForceDelay,iEchoCancelling,iIEC1107Compatible,encryptor, halfDuplexController, software7E1, logger);
        this.dontSendLogOffCommand = dontSendBreakCommand;
    }

    /**
     * Set the FlagIEC1107 connection as disconnected without sending a break to the device.
     *
     * @throws NestedIOException
     * @throws ConnectionException
     */
    public void disconnectMACWithoutBreak() throws ConnectionException, NestedIOException {
        if (boolFlagIEC1107Connected == true) {
            try {
                delayAndFlush(DELAY_AFTER_BREAK); // KV 06072004
                boolFlagIEC1107Connected = false;
                return;
            }
            catch (ConnectionException e) {
                try {
                    flushInputStream();
                }
                catch (ConnectionException ex) {
                    throw new FlagIEC1107ConnectionException("disconnectMAC() error, ConnectionException, " + e.getMessage() + ", ConnectionException, " + ex.getMessage());
                }
                throw new FlagIEC1107ConnectionException("disconnectMAC() error, ConnectionException, " + e.getMessage());
            }
        } // if (boolFlagIEC1107Connected==true)
    }

    /**
     * Method that requests a MAC disconnect for the IEC1107 layer.
     */
    public void disconnectMAC() throws NestedIOException, FlagIEC1107ConnectionException {
        if (boolFlagIEC1107Connected == true) {
            try {
                //byte[] buffer = {(byte)SOH,(byte)0x42,(byte)0x30,(byte)ETX,(byte)0x71};
                //sendRawData(buffer);
                sendBreak(); // KV 06072004
                delayAndFlush(DELAY_AFTER_BREAK); // KV 06072004
                boolFlagIEC1107Connected = false;
                return;
            }
            catch (ConnectionException e) {
                try {
                    flushInputStream();
                }
                catch (ConnectionException ex) {
                    throw new FlagIEC1107ConnectionException("disconnectMAC() error, ConnectionException, " + e.getMessage() + ", ConnectionException, " + ex.getMessage());
                }
                throw new FlagIEC1107ConnectionException("disconnectMAC() error, ConnectionException, " + e.getMessage());
            }
        } // if (boolFlagIEC1107Connected==true)
    } // public void disconnectMAC() throws FlagIEC1107ConnectionException

    /**
     * Method that requests a MAC disconnect for the IEC1107 layer.
     */
    public void sendBreak() throws NestedIOException, FlagIEC1107ConnectionException {
        try {
            byte[] buffer = {(byte) SOH, (byte) 0x42, (byte) 0x30, (byte) ETX, (byte) 0x71};
            sendRawData(buffer);
            return;
        }
        catch (ConnectionException e) {
            try {
                flushInputStream();
            }
            catch (ConnectionException ex) {
                throw new FlagIEC1107ConnectionException("disconnectMAC() error, ConnectionException, " + e.getMessage() + ", ConnectionException, " + ex.getMessage());
            }
            throw new FlagIEC1107ConnectionException("sendBreak() error, " + e.getMessage());
        }
    } // public void sendBreak() throws FlagIEC1107ConnectionException

    /**
     * Method that requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     */
    public MeterType connectMAC() throws IOException, FlagIEC1107ConnectionException {
        return connectMAC(strIdentConfig, strPass, iSecurityLevel, meterID, baudrate);
    }

    public MeterType connectMAC(String strIdentConfig, String strPass, int iSecurityLevel, String meterID) throws IOException, FlagIEC1107ConnectionException {
        return connectMAC(strIdentConfig, strPass, iSecurityLevel, meterID, 0);
    }

    public MeterType connectMAC(String strIdentConfig, String strPass, int iSecurityLevel, String meterID, int baudrate) throws IOException, FlagIEC1107ConnectionException {

        this.strIdentConfig = strIdentConfig;
        this.strPass = strPass;
        this.iSecurityLevel = iSecurityLevel;
        this.meterID = meterID;
        this.baudrate = baudrate;

        if (boolFlagIEC1107Connected == false) {
            MeterType meterType;

            try {
                // KV 18092003
                if (hhuSignOn == null) {
                    meterType = signOn(strIdentConfig, meterID);
                } else {
                    meterType = hhuSignOn.signOn(strIdentConfig, meterID, baudrate);
                }
                boolFlagIEC1107Connected = true;
                prepareAuthentication(strPass);
                sessionState = STATE_PROGRAMMINGMODE;
                return meterType;
            }
            catch (FlagIEC1107ConnectionException e) {
                throw new FlagIEC1107ConnectionException("connectMAC(), FlagIEC1107ConnectionException " + e.getMessage());
            }
            catch (ConnectionException e) {
                throw new FlagIEC1107ConnectionException("connectMAC(), ConnectionException " + e.getMessage());
            }
        } // if (boolFlagIEC1107Connected==false

        return null;

    } // public MeterType connectMAC() throws HDLCConnectionException

    protected MeterType signOn(String strIdentConfig, String meterID) throws IOException, NestedIOException, FlagIEC1107ConnectionException {
        int retries = 0;
        while (true) {
            try {
                String str = "/?" + meterID + "!\r\n";
                sendRawData(str.getBytes());
                // KV 16122003
                String strIdent = receiveIdent(strIdentConfig);

                /* Response on opening msg must
                 *  - start with '/'
                 *  - be longer then 6 chars
                 * otherwhise troubles => trigger retry */
                if ((strIdent.charAt(0) != '/') || (strIdent.length() < 6)) {
                    String msg = "";
                    msg += "FlagIEC1107Connection.signOn() ";
                    msg += "unexpected answer on opening message: ";
                    msg += "'" + strIdent + "'";
                    throw new ConnectionException(msg);
                }

                if (iIEC1107Compatible == 1) {
                    // protocol mode C, programming mode
                    byte[] ack = {(byte) ACK, (byte) 0x30, (byte) strIdent.charAt(4), (byte) 0x31, (byte) 0x0D, (byte) 0x0A};
                    sendRawData(ack);
                } else {
                    // special case for Elster A1700 meter
                    byte[] ack = {(byte) ACK, (byte) 0x30, (byte) strIdent.charAt(4), (byte) 0x36, (byte) 0x0D, (byte) 0x0A};
                    sendRawData(ack);
                }

                return new MeterTypeImpl(strIdent);
            }
            catch (StringIndexOutOfBoundsException e) {
                throw new FlagIEC1107ConnectionException("signOn() error, " + e.getMessage());
            }
            catch (ConnectionException e) {
                if (retries++ >= iMaxRetries) {
                    throw new FlagIEC1107ConnectionException("signOn() error iMaxRetries, possibly meter not responding or wrong nodeaddress, " + e.getMessage());
                } else {
                    logErrorMessage(Level.INFO, "signOn() error [retry " + retries + " of " + iMaxRetries + "], " + e.getMessage());
                    if (!dontSendLogOffCommand) {
                        sendBreak();
                    }
                    delay(DELAY_AFTER_BREAK); // KV 06102003
                }
            }
        }

    } // private MeterType signOn(String strIdentConfig, String meterID) throws NestedIOException,FlagIEC1107ConnectionException

    protected void prepareAuthentication(String strPass) throws NestedIOException, FlagIEC1107ConnectionException {
        int iRetries = 0;

        while (true) {
            try {
                // here, mac is connected for HHU or modem
                if (iSecurityLevel == 0) {
                    receiveData();
                } else if (iSecurityLevel == 1) {
                    skipCommandMessage(); // P0
                    // KV 06072004
                    if (strPass == null) {
                        throw new FlagIEC1107ConnectionException("FlagIEC1107Connection: invalid SecurityLevel 1 with a null password!", SECURITYLEVEL_ERROR);
                    }
                    authenticationCommand = LOGON_PROCEDURE_1;
                    authenticationData = buildData(strPass);
                } else if (iSecurityLevel == 2) {
                    byte[] key = receiveData();
                    authenticationCommand = LOGON_PROCEDURE_2;
                    if (encryptor != null) {
                        if (key.length == 0) {
                            throw new FlagIEC1107ConnectionException("FlagIEC1107Connection: Security issue error, P0 argument is empty (" + new String(key) + ")", SECURITYLEVEL_ERROR);
                        }
                        authenticationData = buildData(encryptor.encrypt(strPass, new String(key)));
                    } else {
                        authenticationData = buildData(strPass);
                    }
                } else if (iSecurityLevel == 3) {
                    byte[] key = receiveData();
                    authenticationCommand = LOGON_PROCEDURE_2;

                    // Elster specific, security level 3 sends P2(0000000000000000) first and after that i
                    authenticationData = buildData("0000000000000000");
                    authenticate();

                    if (encryptor != null) {
                        if (key.length == 0) {
                            throw new FlagIEC1107ConnectionException("FlagIEC1107Connection: Security issue error, P0 argument is empty (" + new String(key) + ")", SECURITYLEVEL_ERROR);
                        }
                        authenticationData = buildData(encryptor.encrypt(strPass, new String(key)));
                    } else {
                        authenticationData = buildData(strPass);
                    }
                } else {
                    throw new FlagIEC1107ConnectionException("FlagIEC1107Connection: invalid SecurityLevel", SECURITYLEVEL_ERROR);
                }

                authenticate();
                return;

            }
            catch (FlagIEC1107ConnectionException e) {
                if (e.getReason() == SECURITYLEVEL_ERROR) {
                    throw e;
                } else if (e.getReason() == TIMEOUT_ERROR) {
                    throw new FlagIEC1107ConnectionException("Authentication response timeout after (" + iMaxRetries + ") retries! Possibly wrong password!, " + e.getMessage());
                } else if (iRetries++ >= iMaxRetries) {
                    throw new FlagIEC1107ConnectionException("Authentication error after (" + iMaxRetries + ") retries! Possibly wrong password!, " + e.getMessage());
                } else {
                    logErrorMessage(Level.INFO, "Authentication error [retry " + iRetries + " of " + iMaxRetries + "], " + e.getMessage());
                    if (!dontSendLogOffCommand) {
                        sendBreak();
                    }
                    delay(DELAY_AFTER_BREAK);
                }
            }
            catch (IOException e) {
                throw new NestedIOException(e, "Authentication error! Possibly wrong password!");
            }

        } // while(true)

    } // private prepareAuthentication() throws FlagIEC1107ConnectionException

    public void authenticate() throws NestedIOException, FlagIEC1107ConnectionException {

        int iRetries = 0;
        try {
            if (iSecurityLevel != 0) {
                while (true) {
                    echoByteArrayOutputStream.reset();
                    sendRawCommandFrame(authenticationCommand, authenticationData); // logon using securitylevel
                    String resp = receiveString();
                    if (resp.compareTo("ACK") == 0) {
                        break;
                    } else if (resp.compareTo("B0") == 0) {
                        throw new IOException(resp + " received");
                    } else if (resp.compareTo("(ERR1)") == 0) {
                        throw new IOException(resp + " received");
                    } else if (resp.indexOf("(ER") != -1) { // IskraEmeco errors...
                        throw new IOException(resp + " received");
                    }
                    }
                }
            }
        catch (FlagIEC1107ConnectionException e) {
            if (e.getReason() == SECURITYLEVEL_ERROR) {
                throw e;
            } else if (e.getReason() == TIMEOUT_ERROR) {
                throw e;
            } else if (iRetries++ >= iMaxRetries) {
                throw new FlagIEC1107ConnectionException("Authentication error! Possibly wrong password! (error iMaxRetries), " + e.getMessage());
            } else {
                logErrorMessage(Level.INFO, "Authentication error [retry " + iRetries + " of " + iMaxRetries + "], " + e.getMessage());
                if (!dontSendLogOffCommand) {
                    sendBreak();
                }
                delay(DELAY_AFTER_BREAK);
            }
        }
        catch (IOException e) {
            throw new NestedIOException(e, "Authentication error! Possibly wrong password!");
        }

    } // public void authenticate(int iSecurityLevel)


    public byte[] dataReadout(String strIdent, String meterID) throws IOException, FlagIEC1107ConnectionException {
        byte[] data = null;
        if (boolFlagIEC1107Connected == false) {
            try {
                data = doDataReadout(strIdent, meterID);
            }
            catch (FlagIEC1107ConnectionException e) {
                throw new FlagIEC1107ConnectionException("dataReadout() error " + e.getMessage());
            }
        } // if (boolFlagIEC1107Connected==false)
        return data;
    } // public void connectMAC() throws HDLCConnectionException

    public byte[] doDataReadout(String strIdentConfig, String meterID) throws IOException, FlagIEC1107ConnectionException {
        int iRetries = 0;
        while (true) {
            try {
                String str = "/?" + meterID + "!\r\n";
                sendRawData(str.getBytes());
                String strIdent = receiveIdent(strIdentConfig);

                // KV 13122007 avoid index out of bound exception and retry...
                if (strIdent.length() < 5) {
                    throw new ConnectionException("Invalid IEC1107 meter identification received!");
                }

                byte[] ack = {(byte) ACK, (byte) 0x30, (byte) strIdent.charAt(4), (byte) 0x30, (byte) 0x0D, (byte) 0x0A};
                sendRawData(ack);
                boolFlagIEC1107Connected = true;
                return (receiveRawData());
            }
            catch (StringIndexOutOfBoundsException e) {
                throw new FlagIEC1107ConnectionException("doDataReadout() error, " + e.getMessage());
            }
            catch (ConnectionException e) {
                if (iRetries++ >= iMaxRetries) {
                    throw new FlagIEC1107ConnectionException("doDataReadout() error iMaxRetries, " + e.getMessage());
                } else {
                    logErrorMessage(Level.INFO, "doDataReadout() error [retry " + iRetries + " of " + iMaxRetries + "], " + e.getMessage());
                    if (!dontSendLogOffCommand) {
                        sendBreak();
                    }
                    delay(DELAY_AFTER_BREAK);
                }

            }
        }

    } // private dataReadout() throws FlagIEC1107ConnectionException


    private byte[] buildData(String strPass) {
        byte[] data = new byte[strPass.getBytes().length + 2];
        int i = 0;
        data[i++] = '(';
        for (int t = 0; t < strPass.getBytes().length; t++) {
            data[i++] = strPass.getBytes()[t];
        }
        data[i++] = ')';
        return data;
    }

    public byte[] parseDataBetweenBrackets(byte[] buffer) throws FlagIEC1107ConnectionException {

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int state = 0;
        for (int i = 0; i < buffer.length; i++) {
            if (state == 0) {
                if (buffer[i] == (byte) '(') {
                    state = 1;
                }
            } else if (state == 1) {
                if (buffer[i] == (byte) ')') {
                    state = 2;
                    break;
                }
                data.write((int) buffer[i]);
            }
        }
        if (state == 2) {
            return data.toByteArray();
        } else {
            throw new FlagIEC1107ConnectionException("FlagIEC1107Connection, parseDataBetweenBrackets, error");
        }
    }

    public static final byte[] WRITE5 = {'W', '5'};
    public static final byte[] WRITE2 = {'W', '2'};
    public static final byte[] READ5 = {'R', '5'};
    public static final byte[] READ6 = {'R', '6'};
    public static final byte[] READ3 = {'R', '3'}; // KV 06072004
    public static final byte[] READ1 = {'R', '1'};
    public static final byte[] READ2 = {'R', '2'};
    public static final byte[] READSTREAM = {'R', 'D'};
    public static final byte[] WRITE1 = {'W', '1'};
    public static final byte[] LOGON_PROCEDURE_1 = {'P', '1'};
    public static final byte[] LOGON_PROCEDURE_2 = {'P', '2'};
    public static final byte[] EXECUTE_COMMAND = {'E', '2'};

    public void sendRawCommandFrame(byte[] command, byte[] rawdata) throws IOException, ConnectionException, FlagIEC1107ConnectionException {
        doSendCommandFrame(command, rawdata, false);
    }

    public String sendRawCommandFrameAndReturn(byte[] command, byte[] rawdata) throws IOException, ConnectionException, FlagIEC1107ConnectionException {
        return doSendCommandFrame(command, rawdata, true);
    }

    private String doSendCommandFrame(byte[] command, byte[] data, boolean returnResult) throws IOException, ConnectionException, FlagIEC1107ConnectionException {
        int iRetries = 0;
        int t, i;
        initTxBuffer(command.length + data.length + 3); // KV 27102004
        byte bChecksum;
        String retVal = null;
        delay(lForceDelay);

        i = 0;
        // KV 27102004
        for (t = 0; t < command.length; t++) {
            getTxBuffer()[i++] = command[t];
        }
        getTxBuffer()[i++] = STX;
        for (t = 0; t < data.length; t++) {
            getTxBuffer()[i++] = data[t];
        }
        getTxBuffer()[i++] = ETX;
        bChecksum = calcChecksum(getTxBuffer());
        getTxBuffer()[getTxBuffer().length - 1] = bChecksum;

        flushEchoBuffer();


        if (command[0] == 'W' || command[0] == 'E') {
            while (true) {
                echoByteArrayOutputStream.reset();
                sendTxBuffer(); // KV 27102004
                resetTxBuffer(); // KV 27102004
                String resp = receiveString();
                if (resp.compareTo("ACK") == 0) {
                    break;
                }

                if (returnResult) {

                    if ((errorSignature != null) && (resp.indexOf(errorSignature) != -1)) {
                        retVal = resp;
                    }

                    break;
                }

                if (iRetries++ >= iMaxRetries) {
                    throw new FlagIEC1107ConnectionException("doSendCommandFrame() error iMaxRetries!");
                } else {
                    logErrorMessage(Level.INFO, "doSendCommandFrame() error [retry " + iRetries + " of " + iMaxRetries + "]");
                }
            }
        } else if ((command[0] == 'R') || (command[0] == 'P')) {
            sendTxBuffer(); // KV 27102004
        } else {
            throw new FlagIEC1107ConnectionException("doSendCommandFrame() error unknown tag!");
        }

        return retVal;

    } // public void doSendCommandFrame(byte bCommand,byte[] data) throws FlagIEC1107ConnectionException

    public void skipCommandMessage() throws NestedIOException, ConnectionException, FlagIEC1107ConnectionException {
        long lMSTimeout;
        int iNewKar;
        byte bState = 0;

        lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

        copyEchoBuffer();

        while (true) {
            if ((iNewKar = readIn()) != -1) {
                if ((bState == 0) && ((byte) iNewKar == SOH)) {
                    bState = 1;
                } else if ((bState == 1) && ((byte) iNewKar == ETX)) {
                    bState = 2;
                } else if (bState == 2) {
                    return;
                }

            } // if ((iNewKar = readIn()) != -1)

            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new FlagIEC1107ConnectionException("skipCommandMessage() timeout error", TIMEOUT_ERROR);
            }

        } // while(true)

    } // public void skipCommandMessage() throws FlagIEC1107ConnectionException

    private static final byte STATE_WAIT_FOR_START = 0;
    private static final byte STATE_WAIT_FOR_LENGTH = 1;
    private static final byte STATE_WAIT_FOR_DATA = 2;
    private static final byte STATE_WAIT_FOR_END = 3;
    private static final byte STATE_WAIT_FOR_CHECKSUM = 4;

    public String receiveString() throws IOException, ConnectionException, FlagIEC1107ConnectionException {
        return new String(receiveRawData());
    }

    /**
     * @return the data without the brackets
     * @throws IOException
     * @throws ConnectionException
     * @throws FlagIEC1107ConnectionException
     */
    public byte[] receiveData() throws IOException, ConnectionException, FlagIEC1107ConnectionException {
        return parseDataBetweenBrackets(doReceiveDataRetry());
    }

    /**
     * @return the data including the brackets
     * @throws IOException
     * @throws ConnectionException
     * @throws FlagIEC1107ConnectionException
     */
    public byte[] receiveRawData() throws IOException, ConnectionException, FlagIEC1107ConnectionException {
        return doReceiveDataRetry();
    }

    // KV 27102004

    private byte[] doReceiveDataRetry() throws IOException, ConnectionException, FlagIEC1107ConnectionException {
        int retries = 0;
        while (true) {
            try {
                return doReceiveData();
            }
            catch (FlagIEC1107ConnectionException e) {
                if ((retries++ < iMaxRetries) && (getTxBuffer() != null) && ((e.getReason() == CRC_ERROR) || (e.getReason() == NAK_RECEIVED) || (e.getReason() == TIMEOUT_ERROR) || (e.getReason() == RECONNECT_ERROR))) {
                    //System.out.println("KV_DEBUG> RETRY "+e.getReason()+", txBuffer="+new String(getTxBuffer()));
                    logErrorMessage(Level.INFO, "doReceiveDataRetry error [retry " + retries + " of " + iMaxRetries + "], " + e.getMessage());
                    delayAndFlush(1000);
                    sendTxBuffer();
//                    sendOut(NAK);
                } else {
                    throw e;
                }
            }
        }
    }

    private byte[] doReceiveData() throws IOException, ConnectionException, FlagIEC1107ConnectionException {
        long lMSTimeout, lMSTimeoutInterFrame;
        int iNewKar;
        int iState;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        byte calculatedChecksum;
        boolean end;

        // init
        iState = STATE_WAIT_FOR_START;
        end = false;
        lMSTimeout = System.currentTimeMillis() + TIMEOUT;
        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
        resultArrayOutputStream.reset();
        byteArrayOutputStream.reset();

        if (DEBUG == 1) {
            System.out.println("doReceiveData(...):");
        }
        copyEchoBuffer();

        while (true) {

            if ((iNewKar = readIn()) != -1) {
                switch (iState) {
                    case STATE_WAIT_FOR_START: {

                        if ((byte) iNewKar == SOH) {
                            iState = STATE_WAIT_FOR_END;
                        }
                        if ((byte) iNewKar == STX) {
                            iState = STATE_WAIT_FOR_END;
                        }
                        if ((byte) iNewKar == ACK) {
                            return ("ACK".getBytes());
                        }
                        // KV 27102004
                        if ((byte) iNewKar == NAK) {
                            if (sessionState != STATE_SIGNON) {
                                throw new FlagIEC1107ConnectionException("doReceiveData() NAK received", NAK_RECEIVED);
                            } else {
                                throw new NestedIOException(new IOException("Probably wrong password! (NAK received)"));
                            }
                        }

                    }
                    break; // STATE_WAIT_FOR_START

                    case STATE_WAIT_FOR_END: {
                        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                        if ((byte) iNewKar == ETX) {
                            end = true;
                            iState = STATE_WAIT_FOR_CHECKSUM;
                        } else if ((byte) iNewKar == EOT) {
                            end = false;
                            iState = STATE_WAIT_FOR_CHECKSUM;
                        }
                        byteArrayOutputStream.write(iNewKar);

                    }
                    break; // STATE_WAIT_FOR_END

                    case STATE_WAIT_FOR_CHECKSUM: {
                        byteArrayOutputStream.write(iNewKar);
                        calculatedChecksum = calcChecksum(byteArrayOutputStream.toByteArray());
                        if (calculatedChecksum == byteArrayOutputStream.toByteArray()[byteArrayOutputStream.toByteArray().length - 1]) {
                            // remove head and tail from byteArrayOutputStream.toByteArray()...
                            byte[] data = new byte[byteArrayOutputStream.toByteArray().length - 2];
                            for (int i = 0; i < (byteArrayOutputStream.toByteArray().length - 2); i++) {
                                data[i] = byteArrayOutputStream.toByteArray()[i];
                            }
                            try {
                                resultArrayOutputStream.write(data);
                                if (isAddCRLF()) {
                                    resultArrayOutputStream.write(0x0d);
                                    resultArrayOutputStream.write(0x0a);
                                }
                            } catch (IOException e) {
                                throw new FlagIEC1107ConnectionException("receiveStreamData(), IOException, " + e.getMessage());
                            }

                            if (end) {
                                byte[] responseData = resultArrayOutputStream.toByteArray();
                                if (new String(responseData).compareTo("B0") == 0) {

                                    // KV 24112008
                                    if (sessionState == STATE_SIGNON) {
                                        return responseData;
                                    }

                                    if (connectCount++ >= 10) {
                                        throw new FlagIEC1107ConnectionException("connectMAC(), max nr of reconnects reached!", PROTOCOL_ERROR);
                                    }
                                    txBuffer2 = new byte[getTxBuffer().length];
                                    System.arraycopy(getTxBuffer(), 0, txBuffer2, 0, txBuffer2.length);
                                    disconnectMAC();
                                    connectMAC();
                                    initTxBuffer(txBuffer2.length);
                                    System.arraycopy(txBuffer2, 0, getTxBuffer(), 0, txBuffer2.length);
                                    throw new FlagIEC1107ConnectionException("doReceiveData() reconnect error", RECONNECT_ERROR);
                                } else {
                                    return responseData;
                                }
                            }

                            // init
                            iState = STATE_WAIT_FOR_START;
                            lMSTimeout = System.currentTimeMillis() + TIMEOUT;
                            lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;
                            byteArrayOutputStream.reset();
                            end = false;

                            sendRawData(ACK);
                            copyEchoBuffer(); // KV 07092005 bugfix when ech cancelling...
                        } else {
                            // KV 27102004
                            throw new FlagIEC1107ConnectionException("doReceiveData() bad CRC error", CRC_ERROR);
                        }

                    } //break; // STATE_WAIT_FOR_CRC

                } // switch(iState)

            } // if ((iNewKar = readIn()) != -1)

            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new FlagIEC1107ConnectionException("doReceiveData() response timeout error", TIMEOUT_ERROR);
            }
            if (((long) (System.currentTimeMillis() - lMSTimeoutInterFrame)) > 0) {
                throw new FlagIEC1107ConnectionException("doReceiveData() interframe timeout error", TIMEOUT_ERROR);
            }


        } // while(true)

    } // public byte[] doReceiveData(String str) throws FlagIEC1107ConnectionException

    // KV 27102004

    public void breakStreamingMode() throws NestedIOException, ConnectionException {
        sendRawData((byte) 0x1B);
        delayAndFlush(3000);
    }

    private static final byte STREAM_STATE_WAIT_FOR_START = 0;
    private static final byte STREAM_STATE_WAIT_FOR_PACKET = 1;
    private static final byte STREAM_STATE_WAIT_FOR_LENGTH = 2;
    private static final byte STREAM_STATE_WAIT_FOR_DATA = 3;
    private static final byte STREAM_STATE_WAIT_FOR_END = 4;
    private static final byte STREAM_STATE_WAIT_FOR_CRC = 5;

    public byte[] receiveStreamData() throws NestedIOException, ConnectionException, FlagIEC1107ConnectionException {
        long lMSTimeout, lMSTimeoutInterFrame;
        int iNewKar;
        int state = STREAM_STATE_WAIT_FOR_START;
        int length = 0;
        int packetNR = 0;
        int count = 0;
        boolean end = false;
        ByteArrayOutputStream brutodata = new ByteArrayOutputStream();
        ByteArrayOutputStream nettodata = new ByteArrayOutputStream();
        ByteArrayOutputStream alldata = new ByteArrayOutputStream();


        brutodata.reset();
        nettodata.reset();
        alldata.reset();

        lMSTimeout = System.currentTimeMillis() + TIMEOUT;
        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;

        if (DEBUG == 1) {
            System.out.println("receiveStreamData(...):");
        }

        copyEchoBuffer();

        while (true) {
            if ((iNewKar = readIn()) != -1) {
                brutodata.write(iNewKar);
                switch (state) {
                    case STREAM_STATE_WAIT_FOR_START: {
                        if ((byte) iNewKar == STX) {
                            state = STREAM_STATE_WAIT_FOR_PACKET;
                            count = 0;
                            packetNR = 0;
                            nettodata.reset();
                        }
                    }
                    break; // STREAM_STATE_WAIT_FOR_START

                    case STREAM_STATE_WAIT_FOR_PACKET: {
                        packetNR |= (((byte) iNewKar & 0xFF) << (8 * count));
                        //System.out.println("KV_DEBUG> streaming packetNR = "+packetNR);
                        if (count++ >= 1) {
                            state = STREAM_STATE_WAIT_FOR_LENGTH;
                        }
                    }
                    break; // STREAM_STATE_WAIT_FOR_PACKET

                    case STREAM_STATE_WAIT_FOR_LENGTH: {
                        length = iNewKar & 0xFF;
                        //System.out.println("KV_DEBUG> streaming length = "+length);
                        count = 0;
                        state = STREAM_STATE_WAIT_FOR_DATA;
                    }
                    break; // STREAM_STATE_WAIT_FOR_LENGTH

                    case STREAM_STATE_WAIT_FOR_DATA: {
                        nettodata.write(iNewKar);
                        if (count++ >= length) {
                            state = STREAM_STATE_WAIT_FOR_END;
                        }
                    }
                    break; // STREAM_STATE_WAIT_FOR_DATA

                    case STREAM_STATE_WAIT_FOR_END: {
                        lMSTimeoutInterFrame = System.currentTimeMillis() + iProtocolTimeout;

                        if ((byte) iNewKar == ETX) {
                            end = false;
                        } else if ((byte) iNewKar == EOT) {
                            end = true;
                        } else {
                            throw new FlagIEC1107ConnectionException("receiveStreamData() invalid end flag", PROTOCOL_ERROR); // KV 27102004
                        }

                        state = STREAM_STATE_WAIT_FOR_CRC;
                        count = 0;
                    }
                    break; // STREAM_STATE_WAIT_FOR_END

                    case STREAM_STATE_WAIT_FOR_CRC: {
                        if (count++ >= 1) {
                            if (CRCGenerator.calcCRC(brutodata.toByteArray()) == 0) {
                                if (nettodata.toByteArray().length != (length + 1)) {
                                    throw new FlagIEC1107ConnectionException("receiveStreamData() nettodata invalid length", PROTOCOL_ERROR); // KV 27102004
                                }
                                try {
                                    alldata.write(nettodata.toByteArray());
                                } catch (IOException e) {
                                    throw new FlagIEC1107ConnectionException("receiveStreamData(), IOException, " + e.getMessage(), PROTOCOL_ERROR); // KV 27102004
                                }
                                if (end) {
                                    alldata.write(255); // 0xFF end of data toevoegen!
                                    return alldata.toByteArray();
                                } else {
                                    state = STREAM_STATE_WAIT_FOR_START;
                                }
                            } else {
                                throw new FlagIEC1107ConnectionException("receiveStreamData() bad crc", CRC_ERROR); // KV 27102004
                            }
                        }

                    }
                    break; // STREAM_STATE_WAIT_FOR_CRC

                } // switch(iState)

            } // if ((iNewKar = readIn()) != -1)

            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new FlagIEC1107ConnectionException("receiveStreamData() response timeout error", TIMEOUT_ERROR);
            }
            if (((long) (System.currentTimeMillis() - lMSTimeoutInterFrame)) > 0) {
                throw new FlagIEC1107ConnectionException("receiveStreamData() interframe timeout error", TIMEOUT_ERROR);
            }

        } // while(true)

    } // public byte[] receiveStreamData(String str) throws FlagIEC1107ConnectionException

    public String receiveIdent(String str) throws NestedIOException, ConnectionException, FlagIEC1107ConnectionException {
        long lMSTimeout;
        int iNewKar;
        String strIdent = "";
        byte[] convert = new byte[1];

        lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

        copyEchoBuffer();
        String convertstr;

        while (true) {

            if ((iNewKar = readIn()) != -1) {
                if ((byte) iNewKar == NAK) {
                    sendBreak();
                }

                convert[0] = (byte) iNewKar;
                convertstr = new String(convert);
                strIdent += convertstr;
                if (convertstr.compareTo("\\") == 0) {
                    strIdent += convertstr;
                }

                // KV 15122003 if deviceid is different from null and not empty, use it to compare
                // with the received deviceid.
                if ((str != null) && ("".compareTo(str) != 0)) {
                    if (strIdent.compareTo(str) == 0) {
                        return strIdent; // KV 16122003
                    }
                    // KV 16122003
                    else if ((byte) iNewKar == 0x0A) {
                        throw new FlagIEC1107ConnectionException("receiveIdent() device id mismatch!");
                    }
                } else {
                    if ((byte) iNewKar == 0x0A) {
                        return strIdent; // KV 16122003
                    }
                }
            } // if ((iNewKar = readIn()) != -1)

            if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new FlagIEC1107ConnectionException("receiveIdent() timeout error", TIMEOUT_ERROR);
            }

        } // while(true)

    } // public void receiveIdent(String str) throws FlagIEC1107ConnectionException

    protected void logErrorMessage(Level level, String msg) {
        if (logger != null) {
            logger.log(level, msg);
        }
    }

    //***********************************************************************************
    // KV 27102004
    // TX buffer management

    private byte[] getTxBuffer() {
        return txBuffer;
    }

    private void resetTxBuffer() {
        txBuffer = null;
    }

    private void initTxBuffer(int length) {
        txBuffer = new byte[length];
    }

    private void sendTxBuffer() throws ConnectionException {
        // KV 12082005
        byte[] data = new byte[txBuffer.length + 1];
        data[0] = SOH;
        for (int i = 1; i < data.length; i++) {
            data[i] = txBuffer[i - 1];
        }
        sendOut(data);
//        sendOut(SOH);
//        sendOut(txBuffer);
    }

    // KV 18092003
    protected HHUSignOn hhuSignOn = null;

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
        this.hhuSignOn = hhuSignOn;
    }

    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;
    }

    public int getIEchoCancelling() {
        return iEchoCancelling;
    }

    public void setErrorSignature(String errorSignature) {
        this.errorSignature = errorSignature;
    }

    private boolean isAddCRLF() {
        return addCRLF;
    }

    /**
     * In case of a R6 command, some meters do not add CRLF at the end of a ACK-ed block of data.
     * This method enabled adding a CRLF at the end of a ACK-ed block of data.
     * default false
     */
    public void setAddCRLF(boolean addCRLF) {
        this.addCRLF = addCRLF;
    }

    public void delayAndFlush(long delay) throws NestedIOException, ConnectionException {
        super.delayAndFlush(delay);
    }

    public String toString() {
        final String crlf = "\r\n";

        StringBuffer sb = new StringBuffer();
        sb.append("FlagIEC1107Connection").append(crlf);
        sb.append(" > addCRLF = ").append(addCRLF).append(crlf);
        sb.append(" > authenticationCommand = ").append(authenticationCommand).append(crlf);
        sb.append(" > authenticationData = ").append(authenticationData).append(crlf);
        sb.append(" > baudrate = ").append(baudrate).append(crlf);
        sb.append(" > boolFlagIEC1107Connected = ").append(boolFlagIEC1107Connected).append(crlf);
        sb.append(" > connectCount = ").append(connectCount).append(crlf);
        sb.append(" > echoByteArrayOutputStream = ").append(echoByteArrayOutputStream).append(crlf);
        sb.append(" > encryptor = ").append(encryptor).append(crlf);
        sb.append(" > errorSignature = ").append(errorSignature).append(crlf);
        sb.append(" > hhuSignOn = ").append(hhuSignOn).append(crlf);
        sb.append(" > iEchoCancelling = ").append(iEchoCancelling).append(crlf);
        sb.append(" > iIEC1107Compatible = ").append(iIEC1107Compatible).append(crlf);
        sb.append(" > iMaxRetries = ").append(iMaxRetries).append(crlf);
        sb.append(" > iProtocolTimeout = ").append(iProtocolTimeout).append(crlf);
        sb.append(" > iSecurityLevel = ").append(iSecurityLevel).append(crlf);
        sb.append(" > lForceDelay = ").append(lForceDelay).append(crlf);
        sb.append(" > meterID = ").append(meterID).append(crlf);
        sb.append(" > sessionState = ").append(sessionState).append(crlf);
        sb.append(" > software7E1 = ").append(software7E1).append(crlf);
        sb.append(" > strIdentConfig = ").append(strIdentConfig).append(crlf);
        sb.append(" > strPass = ").append(strPass).append(crlf);
        return sb.toString();
    }

}
