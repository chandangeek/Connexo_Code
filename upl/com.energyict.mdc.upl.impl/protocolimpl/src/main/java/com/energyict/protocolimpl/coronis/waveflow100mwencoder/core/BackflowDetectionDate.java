package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.util.Date;

import com.energyict.protocol.ProtocolUtils;

public class BackflowDetectionDate extends AbstractParameter {

	int portId;
	
	/**
	 * Detection date for the backflow...
	 */
	Date date=null;
	
	final Date getDate() {
		return date;
	}

	BackflowDetectionDate(WaveFlow100mW waveFlow100mW, int portId) {
		super(waveFlow100mW);
		this.portId=portId;
	}

	@Override
	ParameterId getParameterId() {
		return portId==0?ParameterId.BackflowDetectionDatePortA:ParameterId.BackflowDetectionDatePortB;
	}

	@Override
	void parse(byte[] data) throws IOException {
		long l = ProtocolUtils.getLong(data, 0, 7);
		if (l != 0x01010101010101L) {
			date = TimeDateRTCParser.parse(data, getWaveFlow100mW().getTimeZone()).getTime();
		}
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}
	
}

