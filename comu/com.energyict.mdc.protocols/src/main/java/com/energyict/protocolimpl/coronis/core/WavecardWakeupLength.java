/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class WavecardWakeupLength extends AbstractEscapeCommand {

	WavecardWakeupLength(ProtocolStackLink protocolStackLink, int wakeupLength) {
		super(protocolStackLink);
		this.wakeupLength=wakeupLength;
	}

	int wakeupLength;
	
	@Override
    EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.WAKEUP_LENGTH;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)(wakeupLength>>8),(byte)wakeupLength};
	}
	
	
}
