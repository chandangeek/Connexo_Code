package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AlarmRoute extends AbstractRadioCommand { 

	
	
	
	AlarmRoute(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	private int alarmConfiguration;
	
	final void setAlarmConfiguration(int alarmConfiguration) {
		this.alarmConfiguration = alarmConfiguration;
	}


	
	@Override
	void parse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			int result = WaveflowProtocolUtils.toInt(dais.readByte());
			if (result != 0) {
				throw new WaveFlow100mwEncoderException("Error updating the alarm route, return code ["+WaveflowProtocolUtils.toHexString(result)+"]!");
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveFlow100mW().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
				}
			}
		}		
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)alarmConfiguration};
	}



	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.AlarmRoute;
	}

	
}
