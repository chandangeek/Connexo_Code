package com.energyict.dlms.cosem.custom;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class DatabaseIDCustomCosem extends Data {

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
			return "DatabaseIDCustomCosem: databaseID="+getDatabaseID();
		}
		catch(IOException e) {
			return "DatabaseIDCustomCosem: not able to evaluate because of "+e.toString();
		}
	}
	
	static final byte[] LN=new byte[]{0,0,96,50,0,0};
	
	public DatabaseIDCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
		
	}
	
	public DatabaseIDCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(int databaseID) throws IOException {
		setValueAttr(new Integer32(databaseID));
    }
    
	public int getDatabaseID() throws IOException {
		return getValueAttr().intValue();
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
