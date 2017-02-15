/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class OperatingMode extends AbstractParameter {

	OperatingMode(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
		// TODO Auto-generated constructor stub
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
