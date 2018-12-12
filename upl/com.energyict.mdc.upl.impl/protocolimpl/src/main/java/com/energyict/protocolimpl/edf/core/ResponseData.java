/*
 * ResponseData.java
 *
 * Created on 20 maart 2006, 17:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

/**
 *
 * @author koen
 */
public class ResponseData {
    
    private byte[] data;
    private int sPDUType;
    
    /** Creates a new instance of ResponseData */
    public ResponseData(byte[] data) {
        this.setData(ProtocolUtils.getSubArray2(data, 1, data.length-1));
        this.setSPDUType((int)data[0]&0xFF);
    }
   
    public String toString() {
        return "ResponseData binary: "+ProtocolUtils.outputHexString(getData())+"\n"+"ResponseData ascii: "+new String(getData());
    }
    
    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    public int getSPDUType() {
        return sPDUType;
    }

    public void setSPDUType(int sPDUType) {
        this.sPDUType = sPDUType;
    }
}
