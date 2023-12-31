package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

abstract class AlarmStatus<T> {

	private byte[] status;
	
	AlarmStatus(byte[] data,WaveFlow100mW waveFlow100mW) throws IOException {
		status = ProtocolUtils.getSubArray2(data,0,3);
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));

		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					waveFlow100mW.getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
				}
			}
		}					
	}
	
	static int size() {
		return 3;
	}
	
	final byte[] getStatus() {
		return status;
	}
	
}
 