package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class SamplingActivationType extends AbstractParameter {
	
	SamplingActivationType(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}
	

	final static int START_IMMEDIATE=0x00;
	final static int START_NEXT_HOUR=0x01;
	
	/**
	 * 0x00 means "Start datalogging instantaneously".
	 * 0x01 means "Start datalogging on the next full hour".
	 */
	int type;

	final int getType() {
		return type;
	}

	final void setType(int type) {
		this.type = type;
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.SamplingActivationType;
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		type = WaveflowProtocolUtils.toInt(data[0]);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)type} ;
	}
}
