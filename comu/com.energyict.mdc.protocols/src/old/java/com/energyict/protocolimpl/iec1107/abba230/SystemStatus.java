/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

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
