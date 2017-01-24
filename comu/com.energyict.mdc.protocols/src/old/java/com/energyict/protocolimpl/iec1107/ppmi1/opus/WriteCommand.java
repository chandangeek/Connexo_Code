package com.energyict.protocolimpl.iec1107.ppmi1.opus;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.io.IOException;

class WriteCommand extends OpusCommand {

	private OpusResponse opusResponse = null;
	private String dataIdentity = null;
	private String packetNumber = "000";
	private String dayNumber = "851";

	private byte[] data;

	public WriteCommand(String dataIdentity, byte[] data, OpusConnection connection) {
		super(connection);
		this.dataIdentity = dataIdentity;
		this.data = data;
	}

	public void execute() throws ConnectionException, IOException {
		MessageComposer iMessage = getOpusConnection().createInstruction(dataIdentity, packetNumber, CtrlChar.WRITE, dayNumber);
		getOpusConnection().sendOut(iMessage);
		getOpusConnection().sendOut(CtrlChar.STX.getByteValue());

		byte[] receive = getOpusConnection().receiveMessage(null);
		checkDefinition(receive);
		opusResponse.setDefinitionMessage(receive);

		getOpusConnection().sendOut(CtrlChar.ACK.getByteValue());
		getOpusConnection().receive(CtrlChar.STX);

		MessageComposer message = getOpusConnection().createDataMessage(dataIdentity, "001", data);
		getOpusConnection().sendOut(message);
		getOpusConnection().sendOut(CtrlChar.EOT.getByteValue());

	}

	/* Z field in definition message must be "W" */
	private void checkDefinition(byte [] rsp) throws IOException {

		String rcvd = toString(rsp, 11, 1);
		if( ! rcvd.equals("W") ) {
			String msg = "Received Z field=" + rcvd + " (expected=W)";
			getOpusConnection().getLogger().severe(msg);
			throw new IOException(msg);
		}

		check(rsp, dataIdentity, 0);

	}

	public OpusResponse getOpusResponse() {
		return opusResponse;
	}

	public void clearOpusResponse() {
		opusResponse = new OpusResponse(getOpusConnection().getTimeZone(), false);

	}
}
