package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class AlarmConfiguration extends AbstractParameter {

	int alarmConfiguration;
	
	final int getAlarmConfiguration() {
		return alarmConfiguration;
	}

	final void setAlarmConfiguration(int alarmConfiguration) {
		this.alarmConfiguration = alarmConfiguration;
	}

	AlarmConfiguration(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.AlarmConfiguration;
	}

	@Override
	void parse(byte[] data) throws IOException {
		alarmConfiguration = WaveflowProtocolUtils.toInt(data[0]);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)alarmConfiguration};
	}

}
