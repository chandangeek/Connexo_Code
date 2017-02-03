package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AlarmStatus {
	
	/**
	 * The alarm status is 3 bytes. Only 3 first bits of the second byte are used to notify power up/down and link fault with meter.
	 */
	static private final int POWERDOWN_FLAG = 0x0004;
	static private final int POWERUP_FLAG = 0x0002;
	static private final int LINKFAULT_FLAG = 0x0001;
	boolean powerDown;
	boolean powerUp;
	boolean linkFaultWithMeter;
	

	AlarmStatus(byte[] data,AbstractDLMS abstractDLMS) throws IOException {
		DataInputStream dais = null;
		try {
			dais = new DataInputStream(new ByteArrayInputStream(data));
			dais.readByte(); // absorb
			int temp = WaveflowProtocolUtils.toInt(dais.readByte());
			powerDown = (temp & POWERDOWN_FLAG) == POWERDOWN_FLAG;
			powerUp = (temp & POWERUP_FLAG) == POWERUP_FLAG;
			linkFaultWithMeter = (temp & LINKFAULT_FLAG) == LINKFAULT_FLAG;
			dais.readByte(); // absorb
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					abstractDLMS.getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
				}
			}
		}					
	}
	
	static int size() {
		return 3;
	}	
	
	final boolean isPowerDown() {
		return powerDown;
	}


	final boolean isPowerUp() {
		return powerUp;
	}


	final boolean isLinkFaultWithMeter() {
		return linkFaultWithMeter;
	}	
}
