package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.*;

public class FirmwareVersion extends AbstractRadioCommand {

	FirmwareVersion(WaveLog waveLog) {
		super(waveLog);
	}

	private int communicationMode;
	private int firmwareVersion;

	public final int getCommunicationMode() {
		return communicationMode;
	}

	public final int getFirmwareVersion() {
		return firmwareVersion;
	}

	@Override
    protected RadioCommandId getRadioCommandId() {
		return RadioCommandId.FirmwareVersion;
	}

	@Override
    protected void parse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));
			dais.readByte(); // skip character 'V' 0x56
			communicationMode = WaveflowProtocolUtils.toInt(dais.readShort());
			firmwareVersion = WaveflowProtocolUtils.toInt(dais.readShort());
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveLog().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
		
	}

	@Override
    protected byte[] prepare() throws IOException {
		return new byte[0];          //return an empty byte array, there's no extra preparation needed to read out the firmware version.
	}
}