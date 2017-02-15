/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This connection sets the AS220 device into Transparent Communication Mode
 * between the optical and electrical communication ports. While the meter is in
 * transparent mode, it shows the message 'TRANSCO' on the LCD. The command
 * defines a time period this mode is active for and the required communication
 * settings like baud rate and data format. Pressing the push button for more
 * than 4 seconds will cancel the transparent mode manually. The maximum baud
 * rate is limited to the maximum baud rate of the optical port (9600Baud).
 *
 * @author gna
 */
public class AS220TransparentConnection extends FlagIEC1107Connection implements
		HHUSignOn, HHUEnabler {

	private static final int MAX_RETRIES = 1;
	private static final int TIMEOUT = 5000;

	private static final Map<Integer, String> BAUDRATES = new HashMap<Integer, String>();
	static {
		BAUDRATES.put(Integer.valueOf(300), "0");
		BAUDRATES.put(Integer.valueOf(600), "1");
		BAUDRATES.put(Integer.valueOf(1200), "2");
		BAUDRATES.put(Integer.valueOf(2400), "3");
		BAUDRATES.put(Integer.valueOf(4800), "4");
		BAUDRATES.put(Integer.valueOf(9600), "5");
	}

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

	/** The password to use */
	private String password;

	/** The used {@link SerialCommunicationChannel} */
	private SerialCommunicationChannel commChannel;

	/**
	 * Constructor
	 *
	 * @param commChannel
	 *            - the serial line to use
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
	public AS220TransparentConnection(SerialCommunicationChannel commChannel,
			int transparentConnectTime, int transparentBaudrate,
			int transparentDataBits, int transparentStopbits,
			int transparentParity, int securityLevel, String password)
			throws ConnectionException {
        this(commChannel, transparentConnectTime, transparentBaudrate, transparentDataBits, transparentStopbits, transparentParity, securityLevel, password, null);

	}

    public AS220TransparentConnection(SerialCommunicationChannel commChannel,
			int transparentConnectTime, int transparentBaudrate,
			int transparentDataBits, int transparentStopbits,
			int transparentParity, int securityLevel, String password,
            Logger logger)
			throws ConnectionException {
		super(commChannel.getInputStream(), commChannel.getOutputStream(),
				TIMEOUT, MAX_RETRIES, 0, 0, 0, false, logger);
		this.transparentConnectTime = transparentConnectTime;
		this.transparentBaudrate = transparentBaudrate;
		this.transparentDatabits = transparentDataBits;
		this.transparentParity = transparentParity;
		this.transparentStopbits = transparentStopbits;
		this.password = password;
		this.securityLevel = securityLevel;
		this.commChannel = commChannel;
        this.logger = logger;
		enableHHUSignOn(commChannel);
	}

	/**
	 * {@inheritDoc}
	 */
	public void enableDataReadout(boolean enabled) {
		// isn't used
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getDataReadout() {
		// isn't used
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReceivedIdent() {
		// isn't used
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMode(int mode) {
		// isn't used
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProtocol(int protocol) {
		// isn't used
	}

	/**
	 * {@inheritDoc}
	 */
	public MeterType signOn(String strIdent, String meterID) throws IOException {
		MeterType metertype = super.connectMAC("", this.password, securityLevel, "");

		sendOut(buildTransparentByteArray());
		delay(500);
		flushInputStream();

		this.commChannel.setParamsAndFlush(9600, SerialCommunicationChannel.DATABITS_8, SerialCommunicationChannel.PARITY_NONE,
				SerialCommunicationChannel.STOPBITS_1);

		changeHHUSettings();

		return metertype;
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
		buffer[i++] = FlagIEC1107Connection.SOH;
		int checksumOffset = i;
		System.arraycopy(FlagIEC1107Connection.WRITE1, 0, buffer, i,
				FlagIEC1107Connection.WRITE1.length);
		i += FlagIEC1107Connection.WRITE1.length;
		buffer[i++] = FlagIEC1107Connection.STX;
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
		buffer[i++] = FlagIEC1107Connection.ETX;
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
	 * {@inheritDoc}
	 */
	public MeterType signOn(String strIdent, String meterID, int baudrate)
			throws IOException {
		MeterType metertype = super.connectMAC("", this.password,
				securityLevel, "", baudrate);

//		sendOut(buildTransparentByteArray());
		delay(500);
		flushInputStream();

//		this.commChannel.setParamsAndFlush(300,
//				SerialCommunicationChannel.DATABITS_8,
//				SerialCommunicationChannel.PARITY_NONE,
//				SerialCommunicationChannel.STOPBITS_1);

		return metertype;
	}

	/**
	 * {@inheritDoc}
	 */
	public MeterType signOn(String strIdent, String meterID, boolean wakeup,
			int baudrate) throws IOException {
		// isn't used
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void enableHHUSignOn(SerialCommunicationChannel commChannel)
			throws ConnectionException {
		HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(
				this.commChannel, TIMEOUT, 1, 300, 0);
		 hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
		 hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
//		hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
//		hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);

		hhuSignOn.enableDataReadout(false);
		super.setHHUSignOn(hhuSignOn);

	}

	public void changeHHUSettings() throws ConnectionException{
		HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(
				this.commChannel, TIMEOUT, 1, 300, 0);
//		 hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
//		 hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
		hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
		hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);

		hhuSignOn.enableDataReadout(false);
		super.setHHUSignOn(hhuSignOn);
	}

	/**
	 * {@inheritDoc}
	 */
	public void enableHHUSignOn(SerialCommunicationChannel commChannel,
			boolean enableDataReadout) throws ConnectionException {
		// isn't used
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] getHHUDataReadout() {
		// isn't used
		return null;
	}
}