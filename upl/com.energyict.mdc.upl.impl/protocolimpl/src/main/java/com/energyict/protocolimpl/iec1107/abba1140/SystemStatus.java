package com.energyict.protocolimpl.iec1107.abba1140;
import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;

/** @author  Koen */

public class SystemStatus {
    
    long value;
    
    /** Creates a new instance of SystemStatus */
    public SystemStatus(byte[] data) throws IOException {
       value = ProtocolUtils.getIntLE(data,0,4); 
    }
    
    public long getValue() {
        return value;
    }
    
}
