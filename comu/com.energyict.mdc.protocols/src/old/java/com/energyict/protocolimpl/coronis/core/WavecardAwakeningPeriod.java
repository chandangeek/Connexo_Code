/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class WavecardAwakeningPeriod extends AbstractEscapeCommand {

	WavecardAwakeningPeriod(ProtocolStackLink protocolStackLink, int awakeningPeriod) {
		super(protocolStackLink);
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
