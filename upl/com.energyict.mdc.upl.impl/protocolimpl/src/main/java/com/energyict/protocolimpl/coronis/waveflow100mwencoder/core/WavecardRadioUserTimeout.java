package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class WavecardRadioUserTimeout extends AbstractEscapeCommand {

	WavecardRadioUserTimeout(ProtocolLink protocolLink, int timeout) {
		super(protocolLink);
		this.timeout=timeout;
	}

	int timeout;
	
	@Override
	EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.RADIO_USER_TIMEOUT;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)timeout};
	}
	
	
}
