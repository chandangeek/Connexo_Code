/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class NumberOfLoggedRecords extends AbstractParameter {

	public NumberOfLoggedRecords(WaveFlow waveFlow) {
		super(waveFlow);
	}

    private int numberOfRecords = 0;

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    @Override
    protected ParameterId getParameterId() {
		return ParameterId.NrOfLoggedRecords;
	}

	@Override
    public void parse(byte[] data) throws IOException {
		numberOfRecords = ProtocolTools.getUnsignedIntFromBytes(data);
	}

	@Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
	}
}
