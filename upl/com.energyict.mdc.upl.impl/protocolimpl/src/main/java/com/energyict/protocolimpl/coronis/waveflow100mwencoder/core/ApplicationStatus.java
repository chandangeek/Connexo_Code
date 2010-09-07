package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;

public class ApplicationStatus extends AbstractParameter {
	
	int status;
	
	ApplicationStatus(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}
	
	final int getStatus() {
		return status;
	}

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
