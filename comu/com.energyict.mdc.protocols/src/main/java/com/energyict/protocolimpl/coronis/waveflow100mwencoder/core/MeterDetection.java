package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;

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
					getWaveFlow100mW().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}
