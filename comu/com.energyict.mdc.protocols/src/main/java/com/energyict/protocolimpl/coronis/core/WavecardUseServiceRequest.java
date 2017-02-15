/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class WavecardUseServiceRequest extends AbstractEscapeCommand {

	WavecardUseServiceRequest(ProtocolStackLink protocolStackLink) {
		super(protocolStackLink);
	}

	@Override
    EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.USE_SERVICE_REQUEST;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}
	
	
}
