package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class ApplicationStatusSevernTrent extends ApplicationStatus {
	
	int status;
	
	ApplicationStatusSevernTrent(WaveFlow100mW waveFlow100mW) {
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
		return ParameterId.ApplicationStatusEncoder;
	}

	@Override
	void parse(byte[] data) throws IOException {
		status = ProtocolUtils.getInt(data,0,1);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)status};
	}
}
