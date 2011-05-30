package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.*;

public class FirmwareVersion extends AbstractRadioCommand {

	FirmwareVersion(WaveFlow waveFlow) {
		super(waveFlow);
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
	RadioCommandId getRadioCommandId() {
		return RadioCommandId.FirmwareVersion;
	}

	@Override
	void parse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			dais.readByte(); // skip character 'V' 0x56
			modeOfTransmission = WaveflowProtocolUtils.toInt(dais.readShort());
			firmwareVersion = WaveflowProtocolUtils.toInt(dais.readShort());
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}
