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
	static public final int EXIST=3;
	static public final int SYNCMESSAGES=4;
	static public final int UPDATEDEVICE=5;
	static public final int LOOKUPRESOURCES=6;
	static public final int TESTCONNECTION=7;
	
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


    
    public void boot() throws IOException {
   		setValueAttr(new Integer8(BOOT));
    }
    
    public void exist() throws IOException {
   		setValueAttr(new Integer8(EXIST));
    }
    
    public void syncMessages() throws IOException {
   		setValueAttr(new Integer8(SYNCMESSAGES));
    }
    
    public void updateDevice() throws IOException {
   		setValueAttr(new Integer8(UPDATEDEVICE));
    }
    public void lookupResources() throws IOException {
   		setValueAttr(new Integer8(LOOKUPRESOURCES));
    }
    public void testConnection() throws IOException {
   		setValueAttr(new Integer8(TESTCONNECTION));
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
    
    public boolean isExist() throws IOException {
   		return getCode() == EXIST;
    }
    
    public boolean isPost() throws IOException {
   		return getCode() == POST;
    }
    
    public boolean isSyncMessages() throws IOException {
   		return getCode() == SYNCMESSAGES;
    }    
    
    public boolean isDeploy() throws IOException {
   		return getCode() == DEPLOY;
    }
    
    public boolean isUpdateDevice() throws IOException {
   		return getCode() == UPDATEDEVICE;
    }    
    public boolean isLookupResources() throws IOException {
   		return getCode() == LOOKUPRESOURCES;
    }    
    public boolean isTestConnection() throws IOException {
   		return getCode() == TESTCONNECTION;
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
