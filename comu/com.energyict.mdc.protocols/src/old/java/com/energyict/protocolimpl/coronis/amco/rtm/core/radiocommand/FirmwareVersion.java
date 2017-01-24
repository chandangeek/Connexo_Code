package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class FirmwareVersion extends AbstractRadioCommand {

	FirmwareVersion(RTM rtm) {
		super(rtm);
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
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
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
					getRTM().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}

	}

	@Override
    protected byte[] prepare() throws IOException {
		return new byte[0];          //return an empty byte array, there's no extra preparation needed to read out the firmware version.
	}
}