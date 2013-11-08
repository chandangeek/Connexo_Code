package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class WavecardRadioUserTimeout extends AbstractEscapeCommand {

	WavecardRadioUserTimeout(ProtocolStackLink protocolStackLink, int timeout) {
		super(protocolStackLink);
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
