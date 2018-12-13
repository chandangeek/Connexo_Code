package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/** @author  Koen */

public class SystemStatus {
    
    long value;
    
    /** Creates a new instance of SystemStatus */
    public SystemStatus(byte[] data, int offset, int length) throws IOException {
        value = ProtocolUtils.getIntLE(data,offset,length);
    }

    public long getValue() {
        return value;
    }
    
}
