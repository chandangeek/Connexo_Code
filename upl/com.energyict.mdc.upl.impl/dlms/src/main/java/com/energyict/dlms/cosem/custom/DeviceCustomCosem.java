package com.energyict.dlms.cosem.custom;

import java.io.IOException;
import java.util.Date;


import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.client.CompoundDataBuilderConnection;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class DeviceCustomCosem extends Data {

	AbstractDataType dataType=null;

	// in case of message null, only a integer8 (byte) is send in the data value
	// in case of message, a struct is coded in the data value containing a integer8 (byte) and a octetstring with the message
	
	static final byte[] LN=new byte[]{0,0,96,120,0,0};
	
	public DeviceCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public DeviceCustomCosem() {
		super(new CompoundDataBuilderConnection(),new ObjectReference(LN));
	}
	
	public DeviceCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public AbstractDataType getValueAttr() throws IOException {
    	if (dataType == null)
    		dataType = super.getValueAttr();
    	return dataType;
    }
    
    public void setValueAttr(AbstractDataType val) throws IOException {
    	dataType = val;
    	super.setValueAttr(dataType);
    }
    
}
