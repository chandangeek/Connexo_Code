package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class ApplicationStatus extends AbstractParameter {

	ApplicationStatus(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	/*
	bit0, Supply voltage supervisor :	Power lost notification
	bit1, Supply voltage	supervisor : Power back notification
	bit2, Link fault with	energy meter
	*/
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
