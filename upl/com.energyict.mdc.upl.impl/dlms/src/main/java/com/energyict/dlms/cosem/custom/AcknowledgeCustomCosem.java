package com.energyict.dlms.cosem.custom;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class AcknowledgeCustomCosem extends Data {

	static public final int SUCCESS=0;
	static public final int FAILURE=1;
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
			return "AcknowledgeCustomCosem: code="+getCode()+", message="+getMessage();
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
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(int code, String message) throws IOException {
    	if (message==null)
    		setValueAttr(new Integer8(code));
    	else {
    		Structure structure = new Structure();
    		structure.addDataType(new Integer8(code));
    		structure.addDataType(OctetString.fromString(message));
    		setValueAttr(structure);
    	}
    }
    
	public int getCode() throws IOException {
		if (dataType == null)
			dataType = getValueAttr();
		if (dataType.isStructure())
			return dataType.getStructure().getDataType(0).intValue();
		else
			return dataType.intValue();
	}
	
	public String getMessage() throws IOException {
		if (dataType == null)
			dataType = getValueAttr();
		if (dataType.isStructure())
			return dataType.getStructure().getDataType(1).getOctetString().stringValue();
		else
			return null;
	}
}
