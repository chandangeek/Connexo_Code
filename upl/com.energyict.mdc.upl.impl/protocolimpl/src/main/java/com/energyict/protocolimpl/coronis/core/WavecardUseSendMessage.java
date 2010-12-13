package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class WavecardUseSendMessage extends AbstractEscapeCommand {

	WavecardUseSendMessage(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	@Override
	EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.USE_SEND_MESSAGE;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}
	
	
}
