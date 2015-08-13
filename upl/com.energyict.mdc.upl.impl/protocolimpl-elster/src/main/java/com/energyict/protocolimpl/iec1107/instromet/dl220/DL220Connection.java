/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Connection class that alows a wakeup befor the IEC1107 Signon
 * 
 * @author gna
 * @since 23-feb-2010
 * 
 */
public class DL220Connection extends FlagIEC1107Connection {

	/**
	 * Default constructor for the DL220 protocol
	 * 
	 * @param inputStream
	 *            - the {@link InputStream} to use
	 * @param outputStream
	 *            - the {@link OutputStream} to use
	 * @param timeout
	 *            - Time in ms. for a request to wait for a response before returning an timeout error.
	 * @param maxRetries
	 *            - nr of retries before fail in case of a timeout or recoverable failure
	 * @param forceDelay
	 *            - delay before send. Some protocols have troubles with fast send/receive
	 * @param echoCancelling
	 *            - echo cancelling on/off
	 * @param compatible
	 *            - behave full compatible or use protocol special features
	 * @param software7E1
	 *            - property to dial with a GSM modem using 7 databits, even parity, 1 stopbit
	 * @throws ConnectionException
	 */
	public DL220Connection(InputStream inputStream, OutputStream outputStream, int timeout, int maxRetries,
			long forceDelay, int echoCancelling, int compatible, boolean software7E1) throws ConnectionException {
		super(inputStream, outputStream, timeout, maxRetries, forceDelay, echoCancelling, compatible, software7E1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MeterType connectMAC(String strIdentConfig, String strPass, int iSecurityLevel, String meterID, int baudrate)
			throws IOException, FlagIEC1107ConnectionException {

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
					meterType = signOn(strIdentConfig, meterID);
				} else {
					meterType = hhuSignOn.signOn(strIdentConfig, meterID, true, baudrate);
				}
				boolFlagIEC1107Connected = true;
				prepareAuthentication(strPass);
				sessionState = STATE_PROGRAMMINGMODE;
				return meterType;
			} catch (FlagIEC1107ConnectionException e) {
				throw new FlagIEC1107ConnectionException("connectMAC(), FlagIEC1107ConnectionException "
						+ e.getMessage());
			} catch (ConnectionException e) {
				throw new FlagIEC1107ConnectionException("connectMAC(), ConnectionException " + e.getMessage());
			}
		} // if (boolFlagIEC1107Connected==false

		return null;

	}

}
