/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class InternalDataCommand extends AbstractRadioCommand {

	InternalDataCommand(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	@Override
	EncoderRadioCommandId getEncoderRadioCommandId() {
		return EncoderRadioCommandId.EncoderInternalData;
	}

	/**
	 * The encoder internal data for Port A and B
	 * If the encoder data is null, no encoder is connected to the port
	 */
	InternalData[] internalDatas=new InternalData[2];
	
	final InternalData[] getInternalDatas() {
		return internalDatas;
	}

	public String toString() {
		return "InternalDataCommand:\n"+"portA=\n"+internalDatas[0]+"\n"+"portB=\n"+internalDatas[1];
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		
		int offset=0;
		int sizeInternalDataPortA = WaveflowProtocolUtils.toInt(data[offset++]);
		int sizeInternalDataPortB = WaveflowProtocolUtils.toInt(data[offset++]);
		
		if (sizeInternalDataPortA != 0) {
			buildInternalData(data,offset,sizeInternalDataPortA,0);
			offset+=sizeInternalDataPortA;
		}
		
		if (sizeInternalDataPortB != 0) {
			buildInternalData(data,offset,sizeInternalDataPortB,1);
			offset+=sizeInternalDataPortB;
		}
	}

	private void buildInternalData(byte[] data, int offset, int size, int portId) throws IOException {
		switch(getEncoderGenericHeader().getEncoderModelInfos()[portId].getEncoderModelType()) {
			case ActarisMBusWater: {
				byte[] encoderDataPortA = WaveflowProtocolUtils.getSubArray(data, offset, size);
				internalDatas[portId] = new ActarisMBusInternalData(encoderDataPortA,getWaveFlow100mW().getLogger(), portId);
			} break;
				
			case ServenTrent: {
				byte[] encoderDataPortA = WaveflowProtocolUtils.getSubArray(data, offset, size);
				internalDatas[portId] = new EncoderInternalData(encoderDataPortA,getWaveFlow100mW().getLogger(), portId);
			} break;
		}
	}
	
	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}
