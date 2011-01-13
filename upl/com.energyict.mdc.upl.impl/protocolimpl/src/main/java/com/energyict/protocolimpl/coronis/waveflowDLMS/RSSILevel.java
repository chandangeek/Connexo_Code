package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;

import com.energyict.protocolimpl.coronis.core.*;

public class RSSILevel extends AbstractRadioCommand {

	RSSILevel(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	private int rssiLevel;
	

	@Override
	RadioCommandId getRadioCommandId() {
		return RadioCommandId.RSSILevel;
	}
	
	@Override
	void parse(byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			
			dais = new DataInputStream(new ByteArrayInputStream(data));
			/*
			    returns 4 bytes:
				Module Type 0x6E
				RSSI Level From 0x00 to 0x20
				Awakening Period 0x01
				Module Type	0x6E			
			 */
			dais.readByte(); // skip module type
			rssiLevel = WaveflowProtocolUtils.toInt(dais.readByte());
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					getProtocolLink().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}		
		
	}

	final int getRssiLevel() {
		return rssiLevel;
	}

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

	
}
