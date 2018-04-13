/*
 * ProfileHeader.java
 *
 */

package com.energyict.protocolimpl.iec1107.a1440;

import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class A1440ProfileHeader {

	private int profileInterval;
	private int nrOfChannels;

	/**
	 * Creates a new instance of ProfileHeader
	 * @param flagIEC1107Connection
	 * @param profileNumber
	 * @param meterExceptionInfo
	 * @throws IOException
	 */
	public A1440ProfileHeader(FlagIEC1107Connection flagIEC1107Connection, int profileNumber, MeterExceptionInfo meterExceptionInfo) throws IOException {
		if (profileNumber == 1) {
			flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5,"X(;)".getBytes());
		} else if (profileNumber == 2) {
			flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5,"Z(;)".getBytes());
		}

		byte[] data = flagIEC1107Connection.receiveRawData();
		DataParser dp = new DataParser();
		String receivedData = dp.parseBetweenBrackets(data,0,0);

		if (receivedData.contains("ERROR")) {
			Object errorMessage = meterExceptionInfo.getExceptionInfo(receivedData);
			if(errorMessage != null) {
				throw new IOException("VDEWProfileHeader, " + errorMessage + "!");
			} else {
				throw new IOException("VDEWProfileHeader, ERROR, possibly requestheader not supported!");
			}
		}

		this.profileInterval = Integer.parseInt(dp.parseBetweenBrackets(data,0,2))*60;
		this.nrOfChannels = Integer.parseInt(dp.parseBetweenBrackets(data,0,3));
	}

	/**
	 * Getter for property profileInterval.
	 * @return Value of property profileInterval.
	 */
	public int getProfileInterval() {
		return this.profileInterval;
	}

	/**
	 * Getter for property nrOfChannels.
	 * @return Value of property nrOfChannels.
	 */
	public int getNrOfChannels() {
		return this.nrOfChannels;
	}


}
