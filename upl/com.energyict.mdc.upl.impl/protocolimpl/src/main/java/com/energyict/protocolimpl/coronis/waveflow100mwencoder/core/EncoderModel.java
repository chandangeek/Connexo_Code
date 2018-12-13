package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderModelInfo.EncoderModelType;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class EncoderModel extends AbstractParameter {


	EncoderModel(WaveFlow100mW waveFlow100mW, final int portId) {
		super(waveFlow100mW);
		this.portId=portId;
	}

	/**
	 * contains the encoder model isd and manufacturer code
	 */
	EncoderModelInfo encoderModelInfo;

	final EncoderModelInfo getEncoderModelInfo() {
		return encoderModelInfo;
	}

	/**
	 * Port A -> portId=0, port B -> portId=1,2,...
	 */
	int portId;

	@Override
	ParameterId getParameterId() {
		if (portId==0) {
			return ParameterId.EncoderModelPortA;
		}
		else {
			return ParameterId.EncoderModelPortB;
		}
	}

	@Override
	void parse(byte[] data) throws IOException {
		EncoderModelType encoderModelType = EncoderModelType.fromId(WaveflowProtocolUtils.toInt(data[0]));

		int manufacturerId=0xff;
		if (WaveflowProtocolUtils.toInt(data[1]) != 0xff) {
			manufacturerId = ProtocolUtils.BCD2hex(data[1]);
		}

		encoderModelInfo = new EncoderModelInfo(encoderModelType,manufacturerId);
	}

	@Override
	byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}
}
