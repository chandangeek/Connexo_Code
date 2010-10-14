package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class WavecardAwakeningPeriod extends AbstractEscapeCommand {

	WavecardAwakeningPeriod(ProtocolLink protocolLink, int awakeningPeriod) {
		super(protocolLink);
		this.awakeningPeriod=awakeningPeriod;
	}

	int awakeningPeriod;
	
	@Override
	EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.AWAKENING_PERIOD;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)awakeningPeriod};
	}
	
	
}
