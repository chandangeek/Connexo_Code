package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;

import com.energyict.protocol.UnsupportedException;

public class FirmwareVersion extends AbstractRadioCommand {

	FirmwareVersion(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	
	private int modeOfTransmission;
	
	private int firmwareVersion;
	
	
	final int getModeOfTransmission() {
		return modeOfTransmission;
	}

	final int getFirmwareVersion() {
		return firmwareVersion;
	}

	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.FirmwareVersion;
	}

	@Override
	void parse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			dais.readByte(); // skip character 'V' 0x56
			modeOfTransmission = Utils.toInt(dais.readShort());
			firmwareVersion = Utils.toInt(dais.readShort());
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
