package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class HDLCFrameParser {

	private byte[] dlmsData;

	private final int HDLC_BODY_OFFSET=9;
	int length;

	final int getLength() {
		return length;
	}

	HDLCFrameParser() {
	}

	void parseFrame(byte[] frame, int offset) throws IOException {

		length = (WaveflowProtocolUtils.toInt(frame[offset + 1])<<8 | WaveflowProtocolUtils.toInt(frame[offset + 2])) & 0x07FF;
		length+=2; // add 7E HDLC delimiters
		if ((frame[offset] != 0x7E) || (frame[offset + length - 1] != 0x7E)) {
			throw new IOException("Missing '7E' HDLC delimiters!");
		}
		else {
			int crc = ProtocolUtils.getInt(frame, offset + length - 3, 2);
			// strip CRC and delimiters
			byte[] hdlcData = ProtocolUtils.getSubArray(frame, offset+1, offset + length - 4);
			if (CRCGenerator.calcHDLCCRC(hdlcData) != crc) {
				throw new IOException("Bad HDLC frame CRC!");
			}
			else {
				dlmsData = ProtocolUtils.getSubArray(frame, offset + HDLC_BODY_OFFSET, offset + length - 4);
			}
		}
	}

	byte[] getDLMSData() {
		return dlmsData;
	}
}
