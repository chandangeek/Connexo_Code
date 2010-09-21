package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class EncoderInternalDataCommand extends AbstractRadioCommand {

	EncoderInternalDataCommand(WaveFlow100mW waveFlow100mW) {
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
	EncoderInternalData[] encoderInternalDatas=new EncoderInternalData[2];
	
	final EncoderInternalData[] getEncoderInternalDatas() {
		return encoderInternalDatas;
	}

	public String toString() {
		return "EncoderInternalDataCommand:\n"+"portA=\n"+encoderInternalDatas[0]+"\n"+"portB=\n"+encoderInternalDatas[1];
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		
		int offset=0;
		int sizeEncoderInternalDataPortA = Utils.toInt(data[offset++]);
		int sizeEncoderInternalDataPortB = Utils.toInt(data[offset++]);
		
		if (sizeEncoderInternalDataPortA != 0) {
			byte[] encoderDataPortA = Utils.getSubArray(data, offset, sizeEncoderInternalDataPortA);
			offset += sizeEncoderInternalDataPortA;
			encoderInternalDatas[0] = new EncoderInternalData(encoderDataPortA,getWaveFlow100mW().getLogger());
		}
		
		if (sizeEncoderInternalDataPortB != 0) {
			byte[] encoderDataPortB = Utils.getSubArray(data, offset, sizeEncoderInternalDataPortB);
			offset += sizeEncoderInternalDataPortB;
			encoderInternalDatas[1] = new EncoderInternalData(encoderDataPortB,getWaveFlow100mW().getLogger());
		}
		
		
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

}
