/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;

import java.io.IOException;

public class EncoderUnit extends AbstractParameter {


	
	EncoderUnit(WaveFlow100mW waveFlow100mW, final int portId) {
		super(waveFlow100mW);
		this.portId=portId;
	}

	/**
	 * Encoder unit and digit info
	 */
	EncoderUnitInfo encoderUnitInfo;
	
	/**
	 * Port A -> portId=0, port B -> portId=1,2,...
	 */
	int portId;
	
	@Override
	ParameterId getParameterId() {
		if (portId==0) {
			return ParameterId.EncoderUnitPortA; 
		}
		else {
			return ParameterId.EncoderUnitPortB;
		}
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		EncoderUnitType encoderUnitType = EncoderUnitType.fromId(WaveflowProtocolUtils.toInt(data[0]));
		int nrOfDigitsBeforeDecimalPoint = WaveflowProtocolUtils.toInt(data[1]);
		encoderUnitInfo = new EncoderUnitInfo(encoderUnitType,nrOfDigitsBeforeDecimalPoint);
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[]{(byte)encoderUnitInfo.getEncoderUnitType().getId(),(byte)encoderUnitInfo.getNrOfDigitsBeforeDecimalPoint()};
	}

	final EncoderUnitInfo getEncoderUnitInfo() {
		return encoderUnitInfo;
	}

	final void setEncoderUnitInfo(EncoderUnitInfo encoderUnitInfo) {
		this.encoderUnitInfo = encoderUnitInfo;
	}
	
}
