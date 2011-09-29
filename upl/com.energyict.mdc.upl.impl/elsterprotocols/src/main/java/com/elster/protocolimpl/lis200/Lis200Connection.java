package com.elster.protocolimpl.lis200;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.*;

/**
 * Connection class that allows a wake up before the IEC1107 Sign on
 * 
 * @author gna
 * @since 23-feb-2010
 * 
 *        copied from DL220Connection by gh
 */
public class Lis200Connection extends FlagIEC1107Connection {

	/**
	 * property to remember identStr for later evaluation
	 */
	private String identifier = "";

	private boolean suppressWakeupSequence;

	/**
	 * Default constructor for the DL220 protocol
	 * 
	 * @param inputStream
	 *            - the {@link java.io.InputStream} to use
	 * @param outputStream
	 *            - the {@link java.io.OutputStream} to use
	 * @param timeout
	 *            - Time in ms. for a request to wait for a response before
	 *            returning an timeout error.
	 * @param maxRetries
	 *            - nr of retries before fail in case of a timeout or
	 *            recoverable failure
	 * @param forceDelay
	 *            - delay before send. Some protocols have troubles with fast
	 *            send/receive
	 * @param echoCancelling
	 *            - echo cancelling on/off
	 * @param compatible
	 *            - behave full compatible or use protocol special features
	 * @param software7e1
	 *            - property to dial with a GSM modem using 7 databits, even
	 *            parity, 1 stopbit
	 * @param suppressWakeupSequence
	 *            - if wakeup sequence should not be send
	 * @throws ConnectionException
	 */
	public Lis200Connection(InputStream inputStream, OutputStream outputStream,
			int timeout, int maxRetries, int forceDelay, int echoCancelling,
			int compatible, boolean software7e1, boolean suppressWakeupSequence)
			throws ConnectionException {
		super(inputStream, outputStream, timeout, maxRetries, forceDelay,
				echoCancelling, compatible, software7e1);
		this.suppressWakeupSequence = suppressWakeupSequence;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MeterType connectMAC(String strIdentConfig, String strPass,
			int iSecurityLevel, String meterID, int baudrate)
			throws IOException {

		this.strIdentConfig = strIdentConfig;
		this.strPass = strPass;
		this.iSecurityLevel = iSecurityLevel;
		this.meterID = meterID;
		this.baudrate = baudrate;

		if (!boolFlagIEC1107Connected) {
			MeterType meterType;

			try {
				// KV 18092003
				if (hhuSignOn == null) {
					/* add wakeup sequence! */
					if (!suppressWakeupSequence) {
						sendWakeupSequence();
					}
					meterType = signOn(strIdentConfig, meterID);
				}
				else {
					meterType = hhuSignOn.signOn(strIdentConfig, meterID, true,
							baudrate);
				}
				boolFlagIEC1107Connected = true;
				prepareAuthentication(strPass);
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

	/**
	 * Send a sequence of 0x00 to the end device (wakeup sequence for elster
	 * lis200 devices)
	 *
	 * @throws java.io.IOException
	 * @throws InterruptedException
	 */
	private void sendWakeupSequence() throws IOException {
		/* send 0 during 2 seconds */
		long endTime = System.currentTimeMillis() + 2000;
		while (System.currentTimeMillis() < endTime) {
			sendRawData(new byte[] { 0 });
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				throw new InterruptedIOException();
			}
		}
	}

	public String getReadIdentifier() {
		return identifier;
	}

}