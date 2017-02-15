/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1.opus;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.io.IOException;

/**
 * @author jme
 *
 */
class ReadCommand extends OpusCommand {

	private OpusResponse opusResponse = null;
	private String dataIdentity = null;
	private String packetNumber = "001";
	private String dayNumber = "000";
	private int nrPackets = 0;
	private boolean isProfileData = false;

	public ReadCommand(String dataIdentity, OpusConnection connection) {
		super(connection);
		this.dataIdentity = dataIdentity;
	}

	public ReadCommand(String dataIdentity, int packetNr, int dayNr, int nrPackets, boolean isProfileData, OpusConnection connection) {
		this(dataIdentity, connection);
		this.packetNumber = getOpusConnection().buildZeroLeadingString(packetNr, 3);
		this.dayNumber = getOpusConnection().buildZeroLeadingString(dayNr, 3);
		this.nrPackets = nrPackets;
		this.isProfileData = isProfileData;
	}

	/* Read sequence:
	 * ->   Wake Up
	 * <-   <AK>
	 * ->   Read Instruction Message
	 * <-   <AK>
	 * ->   <STX>
	 * <-   <SOH> Definition Message
	 * ->   <AK>
	 * <-   <SOH> Data Message x <O
	 * ->   <AK>
	 * ->   <EOT>
	 *
	 * The end of the message is the difficult part.  If the protocol knows
	 * the end is reached, it sends an End of Transmission <EOT>
	 * Else it sends and <ACK>.
	 *
	 * In case of the Profile data, the result has a dynamic lenght.
	 * If the end of the days has been reached, the meter sends an <ETX>
	 */
	public void execute() throws ConnectionException, IOException {
		int packetCount = 1;
		boolean endOfRegister = false;

		MessageComposer iMessage = getOpusConnection().createInstruction(dataIdentity, packetNumber, CtrlChar.READ, dayNumber);
		getOpusConnection().sendOut(iMessage);
		getOpusConnection().sendOut(CtrlChar.STX.getByteValue());
		byte[] rsp = getOpusConnection().receiveMessage(null);
		checkDefinition(rsp);
		opusResponse.setDefinitionMessage(rsp);

		if (opusResponse.isDefinitionMessageValid()) {
			getOpusConnection().sendOut(CtrlChar.ACK.getByteValue());
			getOpusConnection().receiveCtrlChar();
			rsp = getOpusConnection().receiveMessage(CtrlChar.SOH);
			check(rsp, dataIdentity, 1);
			opusResponse.addDataMessage(rsp);
			while (packetCount < nrPackets && !endOfRegister) {
				getOpusConnection().sendOut(CtrlChar.ACK.getByteValue());
				// if SOH, another part is comming
				if (getOpusConnection().receiveCtrlChar() == CtrlChar.SOH) {
					rsp = getOpusConnection().receiveMessage(CtrlChar.SOH);
					check(rsp, dataIdentity, packetCount + 1);
					opusResponse.addDataMessage(rsp);
					packetCount = packetCount + 1;
				} else {
					// if something else (eg <ETX>) the transmission is done
					endOfRegister = true;
				}
			}
			// in the end send an <EOT>, then start new command
			getOpusConnection().sendOut(CtrlChar.EOT.getByteValue());
		}
	}

	/**
	 * Z field in definition message must be "R"
	 *
	 * @param rsp
	 * @throws IOException
	 */
	private void checkDefinition(byte [] rsp) throws IOException {
		String rcvd = toString(rsp, 11, 1);
		if( ! rcvd.equals("R") ) {
			String msg = "Received Z field=" + rcvd + " (expected=R)";
			getOpusConnection().getLogger().severe(msg);
			throw new IOException(msg);
		}
		check(rsp, dataIdentity, 0);
	}

	public OpusResponse getOpusResponse() {
		return opusResponse;
	}

	public void clearOpusResponse() {
		opusResponse = new OpusResponse(getOpusConnection().getTimeZone(), isProfileData);
	}

}

