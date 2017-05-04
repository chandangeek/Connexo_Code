/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

public class NrOfLoggedRecords extends AbstractParameter {

	int nrOfRecords;

	final int getNrOfRecords() {
		return nrOfRecords;
	}

	final void setNrOfRecords(int nrOfRecords) {
		this.nrOfRecords = nrOfRecords;
	}

	NrOfLoggedRecords(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	@Override
	ParameterId getParameterId() {
		return ParameterId.NrOfLoggedRecords;
	}

	@Override
	void parse(byte[] data) throws IOException {
		nrOfRecords=ProtocolUtils.getInt(data, 0, 2);
	}

	@Override
	byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}


}
