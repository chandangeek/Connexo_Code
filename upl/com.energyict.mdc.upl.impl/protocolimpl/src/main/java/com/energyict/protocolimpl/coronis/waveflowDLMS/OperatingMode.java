package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;

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
