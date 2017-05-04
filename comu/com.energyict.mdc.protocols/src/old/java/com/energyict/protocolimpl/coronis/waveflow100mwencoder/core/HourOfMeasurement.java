/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class HourOfMeasurement extends AbstractParameter {
	
	HourOfMeasurement(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}
	
	/**
	 * 0..23 hour in day of week or month to datalog 
	 */
	private int hourId;
	
	final int getHourId() {
		return hourId;
	}

	final void setHourId(int hourId) {
		this.hourId = hourId;
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.HourOfMeasurement;
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		hourId = WaveflowProtocolUtils.toInt(data[0]);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)hourId} ;
	}

}
