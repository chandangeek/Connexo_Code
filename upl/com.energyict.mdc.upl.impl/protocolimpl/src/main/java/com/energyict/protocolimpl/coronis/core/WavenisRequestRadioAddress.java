package com.energyict.protocolimpl.coronis.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class WavenisRequestRadioAddress extends AbstractEscapeCommand {

	private static final int RADIOADDRESS_LENGTH=6;
	
	WavenisRequestRadioAddress(ProtocolStackLink protocolStackLink) {
		super(protocolStackLink);
	}

	byte[] radioAddress;
	
	
	final byte[] getRadioAddress() {
		return radioAddress;
	}

	
	@Override
    EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.WAVENIS_REQUEST_RADIO_ADDRESS;
	}

	@Override
	void parse(byte[] data) throws IOException {
		if (data.length != RADIOADDRESS_LENGTH) {
			throw new EscapeCommandException("Invalid radio address received ["+ ProtocolUtils.outputHexString(data)+"]");
		}
		else {
			radioAddress = data;
		}
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}
	
	
}
