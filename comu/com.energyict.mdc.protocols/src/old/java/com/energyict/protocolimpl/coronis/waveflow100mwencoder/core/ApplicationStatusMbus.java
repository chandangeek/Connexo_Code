/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

public class ApplicationStatusMbus extends ApplicationStatus {

	int status;

	ApplicationStatusMbus(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	@Override
	final int getStatus() {
		return status;
	}

	@Override
	final void setStatus(int status) {
		this.status = status;
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.ApplicationStatusMbus;
	}

	@Override
	void parse(byte[] data) throws IOException {
		status = ProtocolUtils.getInt(data,0,2);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)(status>>8),(byte)status};
	}
}
