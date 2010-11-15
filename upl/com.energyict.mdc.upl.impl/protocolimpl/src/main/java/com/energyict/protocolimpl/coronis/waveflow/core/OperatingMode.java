package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;

public class OperatingMode extends AbstractParameter {

	OperatingMode(WaveFlow waveFlow) {
		super(waveFlow);
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
