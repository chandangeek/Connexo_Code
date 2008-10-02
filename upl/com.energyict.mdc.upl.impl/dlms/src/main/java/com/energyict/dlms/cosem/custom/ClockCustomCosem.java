package com.energyict.dlms.cosem.custom;

import java.io.IOException;
import java.util.Date;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class ClockCustomCosem extends Data {


	AbstractDataType dataType=null;
	
	// in case of message null, only a integer8 (byte) is send in the data value
	// in case of message, a struct is coded in the data value containing a integer8 (byte) and a octetstring with the message

//	public AcknowledgeCustomCosem() {
//		CompoundDataBuilderConnection cosemAPDUBuilder = new CompoundDataBuilderConnection();
//		this(cosemAPDUBuilder);
//	}
	
	public String toString() {
		try {
			return "ClockCustomCosem: date="+getDate();
		}
		catch(IOException e) {
			return "ClockCustomCosem: not able to evaluate because of "+e.toString();
		}
	}
	
	static final byte[] LN=new byte[]{0,0,96,101,0,0};
	
	public ClockCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public ClockCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(Date date) throws IOException {
		setValueAttr(new Unsigned32(date.getTime()/1000));
    }
    
	public Date getDate() throws IOException {
		return new Date(getValueAttr().longValue()*1000);
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
