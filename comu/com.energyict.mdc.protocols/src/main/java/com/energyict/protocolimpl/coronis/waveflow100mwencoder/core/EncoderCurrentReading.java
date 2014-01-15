package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

public class EncoderCurrentReading extends AbstractRadioCommand {

	EncoderCurrentReading(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}


	public String toString() {
		return "EncoderCurrentReading: encoderReading portA="+encoderReadings[0]+", encoderReading portB="+encoderReadings[1]+"\n"+getEncoderGenericHeader();
	}

	/**
	 * The encoder readings
	 */
	private long[] encoderReadings = new long[2];

	final long[] getEncoderReadings() {
		return encoderReadings;
	}

	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.EncoderCurrentReading;
	}

	@Override
	void parse(byte[] data) throws IOException {
		encoderReadings[0] = ProtocolUtils.getLong(data, 0,4);
		encoderReadings[1] = ProtocolUtils.getLong(data, 4,4);
	}

	byte[] prepare() throws IOException {
		return new byte[0];
	}
}
