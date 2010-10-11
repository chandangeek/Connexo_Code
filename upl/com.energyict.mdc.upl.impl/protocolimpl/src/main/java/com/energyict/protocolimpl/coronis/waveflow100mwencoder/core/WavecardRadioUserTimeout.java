package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class WavecardRadioUserTimeout extends AbstractEscapeCommand {

	WavecardRadioUserTimeout(WaveFlow100mW waveFlow100mW, int timeout) {
		super(waveFlow100mW);
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
