/*
 * XID.java
 *
 * Created on 19 juni 2006, 16:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.core;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ENQ extends AbstractSPDU {
    
    private int code;
    private byte[] data;
    private int index = -1;
    
    /** Creates a new instance of XID */
    public ENQ(SPDUFactory sPDUFactory) {
        super(sPDUFactory);
    }
    
    
    protected byte[] prepareBuild() throws IOException {
        byte[] data = new byte[3]; 
        data[0] = SPDU_ENQ;
        data[1] = (byte)getCode();
        if(getIndex() == -1){
        	data[2] = 0x10;
        } else{
//        	data[2] = ProtocolUtils.hex2BCD(getIndex()+10);
//        	data[2] = (byte)(getIndex()+10);
        	setIndex(-1);
        	data[2] = 0x10;
        }
        
//        data[1] = 0x02;
//        data[2] = 0x0A;
        return data;
    }
    
    protected void parse(byte[] data) throws IOException {
        this.setData(data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}


	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
    
} // public class XID extends AabstractSPDU
