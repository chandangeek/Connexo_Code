/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class DayOfWeek extends AbstractParameter {
	
	DayOfWeek(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}
	
	/**
	 * 0..6 = sunday..saturday  (in case of weekly read)
	 * 1..28 (in case of monthly read remark: the day cannot exceed 28)
	 */
	private int dayId;

	final int getDayId() {
		return dayId;
	}

	final void setDayId(int dayId) {
		this.dayId = dayId;
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.DayOfWeek;
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		dayId = WaveflowProtocolUtils.toInt(data[0]);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)dayId} ;
	}

}
