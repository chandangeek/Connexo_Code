package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

abstract class AlarmStatus<T> {

	
	AlarmStatus(byte[] data,WaveFlow100mW waveFlow100mW) throws IOException {
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
					waveFlow100mW.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}					
	}
	
	static int size() {
		return 3;
	}		
}
 