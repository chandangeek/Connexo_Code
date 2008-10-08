package com.energyict.dlms.cosem.custom;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class CommandCustomCosem extends Data {

	static public final int BOOT=0;
	static public final int POST=1;
	static public final int DEPLOY=2;
	
	// ... add new returncodes here...

	AbstractDataType dataType=null;
	
	static final byte[] LN=new byte[]{0,0,96,100,1,0};
	
	public CommandCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public CommandCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
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
    
    public void boot() throws IOException {
   		setValueAttr(new Integer8(BOOT));
    }
    
    public void post() throws IOException {
   		setValueAttr(new Integer8(POST));
    }
    
    public void deploy() throws IOException {
   		setValueAttr(new Integer8(DEPLOY));
    }
    
    public boolean isBoot() throws IOException {
   		return getCode() == BOOT;
    }
    
    public boolean isPost() throws IOException {
   		return getCode() == POST;
    }    
    
    public boolean isDeploy() throws IOException {
   		return getCode() == DEPLOY;
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
