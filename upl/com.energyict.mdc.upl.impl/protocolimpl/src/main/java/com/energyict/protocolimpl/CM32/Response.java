package com.energyict.protocolimpl.CM32;

import com.energyict.protocolimpl.utils.ProtocolUtils;

public class Response {
    
    private byte[] data;
    
    /** Creates a new instance of Response */
    public Response(byte[] data) {
        this.setData(data);
    }

    public String toString() {
        return ProtocolUtils.outputHexString(data);
    }  
    
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
    
}
