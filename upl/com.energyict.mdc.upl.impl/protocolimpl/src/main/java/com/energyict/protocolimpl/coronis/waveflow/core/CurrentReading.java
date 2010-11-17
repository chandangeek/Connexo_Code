package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;

import com.energyict.protocol.*;

public class CurrentReading extends AbstractRadioCommand {

	CurrentReading(WaveFlow waveFlow) {
		super(waveFlow);
	}

	
	public String toString() {
		return "CurrentReading: reading A="+readings[0]+", reading B="+readings[1]+(readings.length>2?", reading C="+readings[2]+", reading D="+readings[3]:"");
	}
	
	/**
	 * The encoder readings
	 */
	private long[] readings;  // indexes for input A,B,C* and D*   (*) C and D depending on the type of waveflow
	
	public final long[] getReadings() {
		return readings;
	}

	@Override
	RadioCommandId getRadioCommandId() {
		return RadioCommandId.GlobalIndexReading;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
		if (data.length > 8) {
			readings = new long[4];
		}
		else {
			readings = new long[2];
		}
			
		
		readings[0] = ProtocolUtils.getLong(data, 0,4);
		readings[1] = ProtocolUtils.getLong(data, 4,4);
		if (data.length >=12) {
			readings[2] = ProtocolUtils.getLong(data, 8,4);
		}
		if (data.length >=16) {
			readings[3] = ProtocolUtils.getLong(data, 12,4);
		}
	}
		
	byte[] prepare() throws IOException {
		return new byte[0];
	}
}
