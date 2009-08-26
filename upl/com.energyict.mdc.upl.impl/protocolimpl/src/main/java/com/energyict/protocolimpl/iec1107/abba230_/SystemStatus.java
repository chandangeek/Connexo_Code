package com.energyict.protocolimpl.iec1107.abba230_;
import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;

/** @author  Koen */

public class SystemStatus {
    
	int[] systemStatus = new int[10];
    long value;
    
    /** Creates a new instance of SystemStatus */
    public SystemStatus(byte[] data) throws IOException {
       value = ProtocolUtils.getIntLE(data,0,4);
       for(int i=0;i<10;i++)
           systemStatus[i] = ProtocolUtils.getInt(data,i,1);
    }
    
    public long getValue() {
        return value;
    }

	public int[] getSystemStatus() {
		return systemStatus;
	}
	public int getSystemStatus(int index) {
		return systemStatus[index];
	}
    
}
