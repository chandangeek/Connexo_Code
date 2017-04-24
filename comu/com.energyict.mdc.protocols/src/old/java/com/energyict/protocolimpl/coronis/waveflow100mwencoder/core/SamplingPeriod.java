/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class SamplingPeriod extends AbstractParameter {
	
	SamplingPeriod(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
		// TODO Auto-generated constructor stub
	}

	enum TimeUnit {
		
		MINUTE1(0,1*60),
		MINUTE5(1,5*60),
		MINUTE15(2,15*60),
		MINUTE30(3,30*60);
		
		int seconds;
		int id;
		
		private TimeUnit(final int id, final int seconds) {
			this.id=id;
			this.seconds=seconds;
		}
		
		static TimeUnit fromId(int id) throws WaveFlow100mwEncoderException {
			for (TimeUnit o : values()) {
				if (o.id == id) {
					return o;
				}
			}
			throw new WaveFlow100mwEncoderException("Invalid timeUnit id ["+WaveflowProtocolUtils.toHexString(id)+"]");
		}
		
		/**
		 * return the sampling period to be programmed in the waveflow module
		 * @param samplingPeriodInSeconds
		 * @return sampling period to be programmed in the waveflow module
		 * @throws WaveFlow100mwEncoderException
		 */
		static int fromSamplingPeriodInSeconds(int samplingPeriodInSeconds) throws WaveFlow100mwEncoderException {
			for (TimeUnit o : values()) {
				int multiplier = samplingPeriodInSeconds/o.seconds;
				if (multiplier <= 63) {
					return (multiplier << 2) | o.id;
				}
			}
			throw new WaveFlow100mwEncoderException("Too large the samplingPeriodInSeconds ["+samplingPeriodInSeconds+"]. Cannot convert to a valid timeunit-multiplier pair (sampling period)");
		}
	}
	
	int samplingPeriodInSeconds;
	
	final int getSamplingPeriodInSeconds() {
		return samplingPeriodInSeconds;
	}

	final void setSamplingPeriodInSeconds(int samplingPeriodInSeconds) {
		this.samplingPeriodInSeconds = samplingPeriodInSeconds;
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.SamplingPeriod;
	}

	
	
	@Override
	void parse(byte[] data) throws IOException {
		int temp = WaveflowProtocolUtils.toInt(data[0]);
		int multiplier = temp >> 2;
		
		if (multiplier == 0) {
			throw new WaveFlow100mwEncoderException("Invalid multiplier ["+multiplier+"] in SamplingPeriod");
		}
		
		int timeUnitId = temp & 0x03;
		
		samplingPeriodInSeconds = TimeUnit.fromId(timeUnitId).seconds*multiplier;
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)TimeUnit.fromSamplingPeriodInSeconds(samplingPeriodInSeconds)} ;
	}

}
