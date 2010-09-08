package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class EncoderModel extends AbstractParameter {

	enum EncoderModelType {
		ServenTrent(0x02,"Severn Trent water meter absolute encoder");
		
		int id;
		String description;
		
		EncoderModelType(final int id, final String description) {
			
		}
		
	}
	
	EncoderModel(WaveFlow100mW waveFlow100mW, final int portId) {
		super(waveFlow100mW);
		this.portId=portId;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	byte[] prepare() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
