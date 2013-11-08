package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.*;

public class AlarmRoute extends AbstractRadioCommand {

	
	
	
	AlarmRoute(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	private int alarmConfiguration;
	
	final void setAlarmConfiguration(int alarmConfiguration) {
		this.alarmConfiguration = alarmConfiguration;
	}

	@Override
	RadioCommandId getRadioCommandId() {
		return RadioCommandId.AlarmRoute;
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			int result = WaveflowProtocolUtils.toInt(dais.readByte());
			if (result != 0) {
				throw new WaveFlowDLMSException("Error updating the alarm route, return code ["+WaveflowProtocolUtils.toHexString(result)+"]!");
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getProtocolLink().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)alarmConfiguration};
	}

	
}
