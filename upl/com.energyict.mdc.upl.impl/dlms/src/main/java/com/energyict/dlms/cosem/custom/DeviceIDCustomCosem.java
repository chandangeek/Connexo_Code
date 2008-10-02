package com.energyict.dlms.cosem.custom;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class DeviceIDCustomCosem extends Data {


	AbstractDataType dataType=null;
	
	// in case of message null, only a integer8 (byte) is send in the data value
	// in case of message, a struct is coded in the data value containing a integer8 (byte) and a octetstring with the message

//	public AcknowledgeCustomCosem() {
//		CompoundDataBuilderConnection cosemAPDUBuilder = new CompoundDataBuilderConnection();
//		this(cosemAPDUBuilder);
//	}
	
	public String toString() {
		try {
			return "DeviceIDCustomCosem: serialID="+getSerialID();
		}
		catch(IOException e) {
			return "DeviceIDCustomCosem: not able to evaluate because of "+e.toString();
		}
	}
	
	static final byte[] LN=new byte[]{0,0,96,1,0,(byte)255};
	
	public DeviceIDCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
		
	}
	
	public DeviceIDCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(String serialId) throws IOException {
		setValueAttr(OctetString.fromString(serialId));
    }
    
	public String getSerialID() throws IOException {
		return getValueAttr().getOctetString().stringValue();
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
