/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MeterDetection extends AbstractRadioCommand {

	MeterDetection(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}



	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.MeterDetection;
	}

	@Override
	void parse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {

			dais = new DataInputStream(new ByteArrayInputStream(data));

		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}

	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}
