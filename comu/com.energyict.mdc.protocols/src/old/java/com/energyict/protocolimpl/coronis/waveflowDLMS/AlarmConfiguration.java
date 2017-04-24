/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;

import java.io.IOException;


/*
 "Alarm Configuration" parameter is used to enable automatically alarm transmission on fault or other anomaly independently.
 Some internals features can be associated to an alarm configuration bit. To be sure that the Alarm frame will
 be sent after fault or problem detection, the user must take care that the corresponding "Operating Mode"
 bit is correctly set.
 */
public class AlarmConfiguration extends AbstractParameter {

	AlarmConfiguration(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	/**
	 * 1 byte alarm configuration
	 * bit2, Supply voltage supervisor : Power lost notification
	 * bit1, Supply voltage supervisor : Power back notification
	 * bit0, Link fault with energy meter
	 */
	private int alarmConfiguration;

	final int getAlarmConfiguration() {
		return alarmConfiguration;
	}

	final void setAlarmConfiguration(int alarmConfiguration) {
		this.alarmConfiguration = alarmConfiguration;
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.AlarmConfiguration;
	}

	@Override
	void parse(byte[] data) throws IOException {
		alarmConfiguration = ProtocolUtils.getInt(data,0,1);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)alarmConfiguration};
	}

}
