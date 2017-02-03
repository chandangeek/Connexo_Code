package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class RSSILevel extends AbstractRadioCommand {

	RSSILevel(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	private int rssiLevel;
    private static final double MAX = 0x20;

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
					getProtocolLink().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
				}
			}
		}		
		
	}

    public double getRssiLevelInPercents() {
        double value = (((double) rssiLevel) / MAX) * 100;
        return Math.round(value * 100.0) / 100.0;
    }

	@Override
	byte[] prepare() throws IOException {
		return new byte[0];
	}

	
}
