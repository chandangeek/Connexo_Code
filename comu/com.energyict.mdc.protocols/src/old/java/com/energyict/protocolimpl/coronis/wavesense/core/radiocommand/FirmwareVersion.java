/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.radiocommand;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class FirmwareVersion extends AbstractRadioCommand {

	FirmwareVersion(WaveSense waveSense) {
		super(waveSense);
	}

	private int modeOfTransmission;
	private int firmwareVersion;

	public final int getModeOfTransmission() {
		return modeOfTransmission;
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
			modeOfTransmission = WaveflowProtocolUtils.toInt(dais.readShort());
			firmwareVersion = WaveflowProtocolUtils.toInt(dais.readShort());
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getWaveSense().getLogger().severe(ProtocolUtils.stack2string(e));
				}
			}
		}

	}

	@Override
    protected byte[] prepare() throws IOException {
		return new byte[0];          //return an empty byte array, there's no extra preparation needed to read out the firmware version.
	}
}