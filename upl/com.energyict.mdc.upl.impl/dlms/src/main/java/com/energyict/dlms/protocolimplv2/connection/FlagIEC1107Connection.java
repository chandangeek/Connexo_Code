package com.energyict.dlms.protocolimplv2.connection;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
* Created by cisac on 6/8/2016.
 * This class should be used for V2 protocols that use a ComChannel
 * and only implements the required methods to set the IEC1107 meter to transparent mode
 * It extends from V1 FlagIEC1107Connection to benefit from some common methods that are already implemented
 * Can be extended to fully port the V1 connection implementation
*/
public class FlagIEC1107Connection extends com.energyict.protocolimpl.iec1107.FlagIEC1107Connection {


    static public final int PROTOCOL_NORMAL = 0;
    static public final int PROTOCOL_HDLC = 2;
    private static final int MAX_RETRIES = 1;
    private static final int TIMEOUT = 30000;

    private static final Map<Integer, String> BAUDRATES = new HashMap<Integer, String>();
    static {
        BAUDRATES.put(Integer.valueOf(300), "0");
        BAUDRATES.put(Integer.valueOf(600), "1");
        BAUDRATES.put(Integer.valueOf(1200), "2");
        BAUDRATES.put(Integer.valueOf(2400), "3");
        BAUDRATES.put(Integer.valueOf(4800), "4");
        BAUDRATES.put(Integer.valueOf(9600), "5");
    }

    private final SerialPortComChannel comChannel;
    /** The time to be in transparent mode */
    private int transparentConnectTime;

    /** The baudrate to use in transparent mode */
    private int transparentBaudrate;

    /** the number of databits to use in transparent mode */
    private int transparentDatabits;

    /** The number of stopbits to use in transparent mode */
    private int transparentStopbits;

    /** The parity to use in transparent mode */
    private int transparentParity;

    /** The securityLevel to use */
    private int securityLevel;
    private HHUSignOnV2 hhuSignOnV2;

    /** The password to use */
    private String password;

    /** The used {@link SerialCommunicationChannel} */
//    private SerialCommunicationChannel commChannel;

    /**
     * Constructor
     *
     * @param comChannel
     *            - the com channel to use
     * @param transparentConnectTime
     *            - the time (in minutes) to keep in transparent mode
     * @param transparentBaudrate
     *            - the baudrate to use in transparent mode
     * @param transparentDataBits
     *            - the number of databits to use in transparent mode
     * @param transparentStopbits
     *            - the number of stopbits to use in transparent mode
     * @param transparentParity
     *            - the parity to use in transparent mode
     * @param securityLevel
     *            - the securityLevel to use
     * @param password
     *            - the password corresponding to the securityLevel
     * @throws ConnectionException
     *             for communication related exceptions
     */
    public FlagIEC1107Connection(SerialPortComChannel comChannel,
                                 int transparentConnectTime, int transparentBaudrate,
                                 int transparentDataBits, int transparentStopbits,
                                 int transparentParity, int securityLevel, String password, HHUSignOnV2 hhuSignOnV2)
            throws ConnectionException {
        this(comChannel, transparentConnectTime, transparentBaudrate, transparentDataBits, transparentStopbits, transparentParity, securityLevel, password, null, hhuSignOnV2);

    }

    public FlagIEC1107Connection(SerialPortComChannel comChannel,
                                 int transparentConnectTime, int transparentBaudrate,
                                 int transparentDataBits, int transparentStopbits,
                                 int transparentParity, int securityLevel, String password,
                                 Logger logger, HHUSignOnV2 hhuSignOnV2)
            throws ConnectionException {
        super(null, null,
                TIMEOUT, MAX_RETRIES, 0, 0, 0, false, logger);
        this.comChannel = comChannel;
        this.transparentConnectTime = transparentConnectTime;
        this.transparentBaudrate = transparentBaudrate;
        this.transparentDatabits = transparentDataBits;
        this.transparentParity = transparentParity;
        this.transparentStopbits = transparentStopbits;
        this.password = password;
        this.securityLevel = securityLevel;
        this.hhuSignOnV2 = hhuSignOnV2;
        this.logger = logger;
        super.setHHUSignOn(hhuSignOnV2);
    }


    /**
     * Construct a byteArray which contains a timeduration of how long the
     * device should be in transparent mode, and default communication settings
     * parameters. (9600 baud, 8 databits, 1 stopbit).
     *
     * @return a byteArray to set the AS220 in transparent mode
     * @throws ConnectionException
     *             if the offset in the checksum calculation is larger then the
     *             data length
     */
    protected byte[] buildTransparentByteArray() throws ConnectionException {
        int i = 0;
        byte[] buffer = new byte[15];
        buffer[i++] = com.energyict.protocolimpl.iec1107.FlagIEC1107Connection.SOH;
        int checksumOffset = i;
        System.arraycopy(com.energyict.protocolimpl.iec1107.FlagIEC1107Connection.WRITE1, 0, buffer, i,
                com.energyict.protocolimpl.iec1107.FlagIEC1107Connection.WRITE1.length);
        i += com.energyict.protocolimpl.iec1107.FlagIEC1107Connection.WRITE1.length;
        buffer[i++] = com.energyict.protocolimpl.iec1107.FlagIEC1107Connection.STX;
        buffer[i++] = 'S';
        buffer[i++] = '0';
        buffer[i++] = 'N';
        buffer[i++] = '(';
        System.arraycopy(getTransparentTimeByteArray(), 0, buffer, i,
                getTransparentTimeByteArray().length);
        i += getTransparentTimeByteArray().length;
        System.arraycopy(getCommunicationParametersByteArray(), 0, buffer, i,
                getCommunicationParametersByteArray().length);
        i += getCommunicationParametersByteArray().length;
        buffer[i++] = ')';
        buffer[i++] = com.energyict.protocolimpl.iec1107.FlagIEC1107Connection.ETX;
        buffer[i++] = calcChecksum(buffer, buffer.length - checksumOffset,
                checksumOffset);
        return buffer;
    }

    /**
     * Convert the time from decimal to hexadecimal chars. ex. 10minutes ->
     * 0x30, 0x41; 20min -> 0x31, 0x34
     *
     * @return
     */
    protected byte[] getTransparentTimeByteArray() {
        byte[] timeByteArray = new byte[2];
        ProtocolUtils.val2HEXascii(this.transparentConnectTime, timeByteArray,
                0);
        return timeByteArray;
    }

    protected byte[] getCommunicationParametersByteArray() {
        byte[] param = new byte[2];

        String b = BAUDRATES.get(Integer.valueOf(this.transparentBaudrate));

        if (b == null) {
            throw new IllegalArgumentException("Invalid baudrate : "
                    + this.transparentBaudrate);
        }

        param[0] = b.getBytes()[0];
        int detailedParam = 0;

        if (this.transparentDatabits == 8) {
            detailedParam |= 0;
        } else if (this.transparentDatabits == 7) {
            detailedParam |= 1;
        } else {
            throw new IllegalArgumentException(
                    "TransparentDataBits property may only be 7 or 8");
        }

        if (this.transparentParity == 0) {
            detailedParam |= 0;
        } else if (this.transparentParity == 1) { // Even parity
            detailedParam |= 2;
        } else if (this.transparentParity == 2) { // Odd parity
            detailedParam |= 6;
        } else {
            throw new IllegalArgumentException(
                    "TransparentParity property may only be 0(no parity), 1(even parity) or 2(odd parity)");
        }

        if (this.transparentStopbits == 1) {
            detailedParam |= 0;
        } else if (this.transparentStopbits == 2) {
            detailedParam |= 8;
        } else {
            throw new IllegalArgumentException(
                    "TransparentStopbits property may only be 1 or 2");
        }
        param[1] = (byte) ProtocolUtils.convertHexMSB(detailedParam);
        return param;
    }

    /**
     * Switch the baud rate and change the line control to 7E1 or 8N1.
     */
    public void switchBaudrate(int baudrate, int protocol) {
        SerialPortConfiguration configuration = comChannel.getSerialPortConfiguration();
        if (protocol == PROTOCOL_NORMAL) {     //Apply baud rate & 7E1
            configuration.setBaudrate(BaudrateValue.valueFor(BigDecimal.valueOf(baudrate)));
            configuration.setNrOfDataBits(NrOfDataBits.SEVEN);
            configuration.setParity(Parities.EVEN);
            configuration.setNrOfStopBits(NrOfStopBits.ONE);
        } else if (protocol == PROTOCOL_HDLC) {      //Apply baud rate & 8N1
            configuration.setBaudrate(BaudrateValue.valueFor(BigDecimal.valueOf(baudrate)));
            configuration.setNrOfDataBits(NrOfDataBits.EIGHT);
            configuration.setParity(Parities.NONE);
            configuration.setNrOfStopBits(NrOfStopBits.ONE);
        }
        comChannel.updatePortConfiguration(configuration);
    }

    public void setMeterToTransparentMode() throws IOException {
        super.connectMAC("", this.password, securityLevel, "");

        startWriting();
        sendOut(buildTransparentByteArray());
        delayAndFlush(1000);

        hhuSignOnV2.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOnV2.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOnV2.enableDataReadout(false);
        super.setHHUSignOn(hhuSignOnV2);
        switchBaudrate(transparentBaudrate, PROTOCOL_HDLC);
        delay(1000);

    }

    @Override
    protected int readIn() throws NestedIOException, ConnectionException {
        if (comChannel.available() != 0) {
            return comChannel.read();
        } else {
            return -1;
        }
    }

    @Override
    protected void copyEchoBuffer() {
        //do nothing
    }

    @Override
    public void delayAndFlush(long delay) throws NestedIOException, ConnectionException {
        delay(delay);
        flushInputStream();
    }

    @Override
    protected void flushInputStream() throws ConnectionException {
        comChannel.startReading();
        while (comChannel.available() != 0) {
            comChannel.read();
        }
    }

    @Override
    protected void startReading() {
        comChannel.startReading();
    }

    @Override
    protected void startWriting() {
        comChannel.startWriting();
    }

    @Override
    public void sendOut(byte[] txbuffer) throws ConnectionException {
        comChannel.write(txbuffer);
    }

    @Override
    public void sendOut(byte txbyte) throws ConnectionException {
        comChannel.write(txbyte);
    }
}
