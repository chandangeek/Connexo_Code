package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;

public class HDLCFrameParser {

	private byte[] dlmsData;
	
	private final int HDLC_BODY_OFFSET=9;
	
	HDLCFrameParser() {
	}

	void parseFrame(byte[] frame) throws IOException {
		if ((frame[0] != 0x7E) || (frame[frame.length-1] != 0x7E)) {
			throw new IOException("Missing '7E' HDLC delimiters!");
		}
		else {
			int crc = ProtocolUtils.getInt(frame, frame.length-3, 2);
			// strip CRC and delimiters
			byte[] hdlcData = ProtocolUtils.getSubArray(frame, 1, frame.length-4);
			if (CRCGenerator.calcHDLCCRC(hdlcData) != crc) {
				throw new IOException("Bad HDLC frame CRC!");
			}
			else {
				dlmsData = ProtocolUtils.getSubArray(frame, HDLC_BODY_OFFSET, frame.length-4);
			}
		}
	}
	
	byte[] getDLMSData() {
		return dlmsData;
	}
}
