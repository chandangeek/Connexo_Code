package com.energyict.dlms.cosem.custom;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class AcknowledgeCustomCosem extends Data {

	// SUCCESS CODES
	static public final int SUCCESS=0;
	static public final int VERSION=1;
	static public final int MESSAGES=2;
	static public final int SCHEDULERS=3;
	
	static public final String[] positiveAcks = new String[]{"SUCCESS","VERSION","MESSAGES","SCHEDULERS"};
	
	// ERROR CODES
	static public final int FAILURE=100;
	static public final int NO_VALID_DBASE_REFERENCE=101;
	static public final int DEVICE_NOT_EXIST=102;
	
	
	
	// ... add new returncodes here...
	

	AbstractDataType dataType=null;
	
	// in case of message null, only a integer8 (byte) is send in the data value
	// in case of message, a struct is coded in the data value containing a integer8 (byte) and a octetstring with the message

//	public AcknowledgeCustomCosem() {
//		CompoundDataBuilderConnection cosemAPDUBuilder = new CompoundDataBuilderConnection();
//		this(cosemAPDUBuilder);
//	}
	
	public String toString() {
		try {
			//return "AcknowledgeCustomCosem: "+(getCode()==SUCCESS?"SUCCESS":"code="+getCode()+", message="+getMessage());
			return "AcknowledgeCustomCosem: "+((getCode()<100)?""+positiveAcks[getCode()]:"code="+getCode()+", message="+getMessage());
		}
		catch(IOException e) {
			return "AcknowledgeCustomCosem: not able to evaluate because of "+e.toString();
		}
	}
	
	static final byte[] LN=new byte[]{0,0,96,100,0,0};
	
	public AcknowledgeCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public AcknowledgeCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
	public boolean isSuccess() throws IOException {
		return getCode()==SUCCESS;
	}
	
	public boolean isMessages() throws IOException {
		return getCode()==MESSAGES;
	}
	
	public boolean isSchedulers() throws IOException {
		return getCode()==SCHEDULERS;
	}
	
	public boolean isVersion() throws IOException {
		return getCode()==VERSION;
	}
	
	public boolean isDeviceNotFound() throws IOException {
		return getCode()==DEVICE_NOT_EXIST;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(int code, String message) throws IOException {
    	if (message==null) {
    		setValueAttr(new Integer8(code));
    	}
    	else {
    		Structure structure = new Structure();
    		structure.addDataType(new Integer8(code));
    		structure.addDataType(OctetString.fromString(message));
    		setValueAttr(structure);
    	}
    }
    
	public int getCode() throws IOException {
		if (getValueAttr().isStructure())
			return getValueAttr().getStructure().getDataType(0).intValue();
		else
			return getValueAttr().intValue();
	}
	
	public String getMessage() throws IOException {
		if (getValueAttr().isStructure())
			return getValueAttr().getStructure().getDataType(1).getOctetString().stringValue();
		else
			return null;
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
