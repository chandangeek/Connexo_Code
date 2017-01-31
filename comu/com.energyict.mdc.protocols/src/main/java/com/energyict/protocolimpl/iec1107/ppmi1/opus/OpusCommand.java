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
abstract class OpusCommand {

	private OpusConnection	opusConnection	= null;

	abstract void execute() throws ConnectionException, IOException;

	abstract OpusResponse getOpusResponse();

	abstract void clearOpusResponse();

	public OpusCommand(OpusConnection opusConnection) {
		this.opusConnection = opusConnection;
	}

	/*
	 * Check Message/Packet for - outstation nr (=nodeId) matches configured
	 * outstation nr - data identity matches requested data identity - packet nr
	 * matches expected packet nr
	 */
	void check(byte[] rsp, String dataId, int pNr) throws IOException {

		int offset = (rsp[0] == CtrlChar.SOH.getByteValue()) ? 1 : 0;

		String rcvd = toString(rsp, offset, 3);
		String xpctd = getOpusConnection().getNodeId();

		if (!rcvd.equals(xpctd)) {
			String msg = "Received NodeId incorrect: " + rcvd;
			getOpusConnection().getLogger().severe(msg);
			throw new IOException(msg);
		}

		rcvd = toString(rsp, (offset + 3), 3);
		xpctd = dataId;

		if (!rcvd.equals(xpctd)) {
			String msg = "Received DataIdentity incorrect: " + rcvd;
			getOpusConnection().getLogger().severe(msg);
			throw new IOException(msg);
		}

		rcvd = toString(rsp, (offset + 6), 3);
		int rcvdPNr = Integer.parseInt(rcvd);

		if (rcvdPNr != pNr) {
			String msg = "Received PacketNr not matching: " + rcvd + " " + pNr;
			getOpusConnection().getLogger().severe(msg);
			throw new IOException(msg);
		}

	}

	public OpusConnection getOpusConnection() {
		return opusConnection;
	}

	/* cut a peace from a byte array and convert to a String */
	String toString(byte[] ba, int start, int length) {
		byte[] rslt = new byte[length];
		System.arraycopy(ba, start, rslt, 0, length);
		return new String(rslt);
	}

}
