package com.energyict.protocolimpl.coronis.wavetalk.core;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;

public class ApplicationStatus extends AbstractApplicationStatus {
	
	int status;
	
	ApplicationStatus(WaveFlow waveFlow) {
		super(waveFlow);
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
		return ParameterId.ApplicationStatus;
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
