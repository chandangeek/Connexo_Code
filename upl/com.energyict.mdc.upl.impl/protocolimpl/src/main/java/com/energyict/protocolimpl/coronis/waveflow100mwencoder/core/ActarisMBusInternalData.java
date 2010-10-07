package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.*;
import java.util.logging.Logger;

import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderModelInfo.EncoderModelType;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderUnitInfo.EncoderUnitType;

public class ActarisMBusInternalData extends InternalData {

	static public final int MBUS_INTERNAL_DATA_LENGTH = 74;

	
	/**
	 * raw string of the internal data
	 */
	private byte[] encoderInternalData;
	
	final public byte[] getEncoderInternalData() {
		return encoderInternalData;
	}	

	public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        return strBuff.toString();
    }

	ActarisMBusInternalData(final byte[] data, final Logger logger) throws IOException {
		
		if (data.length != MBUS_INTERNAL_DATA_LENGTH) {
			throw new WaveFlow100mwEncoderException("Invalid encoder internal data length. Expected length ["+MBUS_INTERNAL_DATA_LENGTH+"], received length ["+data.length+"]");
		}
		
		
		encoderInternalData = data;
		
		DataInputStream dais = null;
		try {
			byte[] temp;
			char c;
			int i;
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					logger.severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
	}
}
