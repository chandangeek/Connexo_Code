package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;
import java.util.Calendar;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class TimeDateRTC extends AbstractRadioCommand {

	private Calendar calendar=null;
	
	static final int SET_OK=0x00;
	static final int SET_ERROR=0xFF;
	
	final Calendar getCalendar() {
		return calendar;
	}


	final void setCalendar(final Calendar calendar) {
		this.calendar = calendar;
	}

	TimeDateRTC(WaveFlow waveFlow) {
		super(waveFlow);
	}

	@Override
	void parse(byte[] data) throws IOException {
		if (getRadioCommandId()==RadioCommandId.ReadCurrentRTC) {
			calendar = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone());
		}
		else {
			// check if write clock succeeded
			if (WaveflowProtocolUtils.toInt(data[0]) == SET_ERROR) {
				getWaveFlow().getLogger().severe("Error setting the RTC in the waveflow device, returned ["+WaveflowProtocolUtils.toInt(data[0])+"]");
			}
		}
	}

	@Override
	byte[] prepare() throws IOException {
		if (getRadioCommandId()==RadioCommandId.ReadCurrentRTC) {
			return new byte[0];
		}
		else {
			return TimeDateRTCParser.prepare(calendar);
		}
	}


	@Override
	RadioCommandId getRadioCommandId() {
		if (calendar==null) {
			return RadioCommandId.ReadCurrentRTC;
		}
		else {
			return RadioCommandId.WriteCurrentRTC;
		}
	}

}
