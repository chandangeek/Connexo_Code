package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class WavecardUseSendFrame extends AbstractEscapeCommand {

	WavecardUseSendFrame(ProtocolStackLink protocolStackLink) {
		super(protocolStackLink);
	}

	@Override
    EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.USE_SEND_FRAME;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}
	
	
}
