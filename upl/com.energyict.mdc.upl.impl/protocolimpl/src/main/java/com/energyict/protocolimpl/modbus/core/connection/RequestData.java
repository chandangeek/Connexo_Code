/*
 * RequestData.java
 *
 * Created on 19 september 2005, 16:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.connection;

import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class RequestData {
    
    private int functionCode;
    private byte[] data;
    
    /** Creates a new instance of RequestData */
    public RequestData(int functionCode) {
        this.setFunctionCode(functionCode);
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(int functionCode) {
        this.functionCode = functionCode;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
    public byte[] getFrameData() {
        if (getData()!=null)
            return ProtocolUtils.concatByteArrays(new byte[]{(byte)getFunctionCode()}, getData());
        else
            return new byte[]{(byte)getFunctionCode()};
    }
    
    
    
}
