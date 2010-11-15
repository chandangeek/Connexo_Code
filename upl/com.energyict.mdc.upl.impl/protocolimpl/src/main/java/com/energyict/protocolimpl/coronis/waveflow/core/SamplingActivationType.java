package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;

public class SamplingActivationType extends AbstractParameter {
	
	SamplingActivationType(WaveFlow waveFlow) {
		super(waveFlow);
	}
	

	/**
	 * start hour when the datalogging has to start.
	 */
	int startHour=0;
	


	final int getStartHour() {
		return startHour;
	}

	final void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.SamplingActivationStartHour;
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		startHour = WaveflowProtocolUtils.toInt(data[0]);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)getStartHour()} ;
	}
}
