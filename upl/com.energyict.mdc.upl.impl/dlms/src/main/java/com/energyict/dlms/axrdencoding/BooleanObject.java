package com.energyict.dlms.axrdencoding;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.protocol.ProtocolUtils;

public class BooleanObject extends AbstractDataType{
	
	private boolean state;
	
	public BooleanObject(boolean state){
		this.state = state;
	}
	
    /** Creates a new instance of Enum */
    public BooleanObject(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_BOOLEAN)
            throw new IOException("BooleanObject, invalid identifier "+berEncodedData[offset]);
        offset++;
        setState(berEncodedData[offset]==0x00?false:true);
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) 
            strBuffTab.append("  ");
        return strBuffTab.toString()+"BooleanObject = "+getState()+"\n";
    }
	
    public boolean getState(){
    	return this.state;
    }
    
    private void setState(boolean state){
    	this.state = state;
    }
    
	protected byte[] doGetBEREncodedByteArray() throws IOException {
        byte[] data = new byte[2];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_BOOLEAN;
        data[1] = (byte)(state?0xff:0x00);
        return data;
	}

	public int intValue() {
		return state?1:0;
	}

	public long longValue() {
		return state?1:0;
	}

	protected int size() {
		return 2;
	}

	public BigDecimal toBigDecimal() {
		return new BigDecimal(intValue());
	}

}
