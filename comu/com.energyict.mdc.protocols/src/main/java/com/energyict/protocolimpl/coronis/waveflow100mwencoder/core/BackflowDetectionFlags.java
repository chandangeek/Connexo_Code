package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

public class BackflowDetectionFlags extends AbstractParameter {

	int portId;

	/**
	 * Detection flags for the backflow...
	 * Back flow detection flags : this word contains 12 relevant bits that express back flow detection in the month
	 * bit0: current month
	 * bit1..12: month -1..-12
	 */
	int flags;

	final int getFlags() {
		return flags;
	}

	BackflowDetectionFlags(WaveFlow100mW waveFlow100mW, int portId) {
		super(waveFlow100mW);
		this.portId=portId;
	}

	@Override
	ParameterId getParameterId() {
		return portId==0?ParameterId.BackflowDetectionFlagsPortA:ParameterId.BackflowDetectionFlagsPortB;
	}

	@Override
	void parse(byte[] data) throws IOException {
		flags = ProtocolUtils.getInt(data,0,2);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}

