package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class WavecardWakeupLength extends AbstractEscapeCommand {

	WavecardWakeupLength(ProtocolLink protocolLink, int wakeupLength) {
		super(protocolLink);
		this.wakeupLength=wakeupLength;
	}

	int wakeupLength;
	
	@Override
	EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.WAKEUP_LENGTH;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)(wakeupLength>>8),(byte)wakeupLength};
	}
	
	
}
