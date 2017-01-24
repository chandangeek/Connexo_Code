package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;

import java.io.IOException;

public class OperatingMode extends AbstractParameter {

	OperatingMode(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	@Override
	ParameterId getParameterId() {
		return null;
	}

	@Override
	void parse(byte[] data) throws IOException {
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}
