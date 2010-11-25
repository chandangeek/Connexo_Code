package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;

public class ApplicationStatus extends AbstractParameter {

	ApplicationStatus(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	private int status;
	
	final int getStatus() {
		return status;
	}

	final void setStatus(int status) {
		this.status = status;
	}

	@Override
	ParameterId getParameterId() {
		// TODO Auto-generated method stub
		return ParameterId.ApplicationStatus;
	}

	@Override
	void parse(byte[] data) throws IOException {
		// TODO Auto-generated method stub
		status = ProtocolUtils.getInt(data,0,1);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)status};
	}

}
