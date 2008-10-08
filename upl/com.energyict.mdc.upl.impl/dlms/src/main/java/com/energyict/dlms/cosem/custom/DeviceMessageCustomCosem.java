package com.energyict.dlms.cosem.custom;

import java.io.IOException;
import java.util.Date;


import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.client.CompoundDataBuilderConnection;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class DeviceMessageCustomCosem extends Data {

	AbstractDataType dataType=null;
    public static final int PENDING = 0;
    public static final int SENT  = 1;
    public static final int CANCELED = 2;
    public static final int WAITING = 3;
    public static final int CONFIRMED = 4;
    public static final int FAILED = 5;  
    public static final int INDOUBT = 6; 	
	
	// in case of message null, only a integer8 (byte) is send in the data value
	// in case of message, a struct is coded in the data value containing a integer8 (byte) and a octetstring with the message

	
    public String toString() {
        // Generated code by ToStringBuilder
        try {
	        StringBuffer strBuff = new StringBuffer();
	        strBuff.append("DeviceMessageCustomCosem:\n");
			strBuff.append("   contents="+getContents()+"\n");
	        strBuff.append("   messageDatabaseId="+getMessageDatabaseId()+"\n");
	        strBuff.append("   messageState="+getMessageState()+"\n");
	        strBuff.append("   releaseDate="+getReleaseDate()+"\n");
	        strBuff.append("   trackingId="+getTrackingId()+"\n");
	        strBuff.append("   userId="+getUserId()+"\n");
	        return strBuff.toString();
		} catch (IOException e) {
			return "DeviceMessageCustomCosem: not able to evaluate because of "+e.toString();
		}
    }	
	
    public boolean isConfirmed() throws IOException {
    	return getMessageState() == CONFIRMED;
    }
    public boolean isFailed() throws IOException {
    	return getMessageState() == FAILED;
    }
    
    public static void main(String[] args) {
        System.out.println(ToStringBuilder.genCode(new DeviceMessageCustomCosem(new Unsigned32(5))));
    }	
	
	static final byte[] LN=new byte[]{0,0,96,110,0,0};
	
	public DeviceMessageCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public DeviceMessageCustomCosem() {
		super(new CompoundDataBuilderConnection(),new ObjectReference(LN));
	}
	
	public DeviceMessageCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(int messageDatabaseId,String contents,Date releaseDate,int userId,String trackingId,int messageState) throws IOException {
		Structure structure = new Structure();
		structure.addDataType(new Integer32(messageDatabaseId));
		structure.addDataType(contents==null?new NullData():OctetString.fromString(contents));
		structure.addDataType(releaseDate==null?null:new Unsigned32(releaseDate.getTime()/1000));
		structure.addDataType(new Integer32(userId));
		structure.addDataType(trackingId==null?new NullData():OctetString.fromString(trackingId));
		structure.addDataType(new Integer8(messageState));
		setValueAttr(structure);
    }
    
    public void setFieldsStateResponse(int messageDatabaseId,int messageState) throws IOException {
		Structure structure = new Structure();
		structure.addDataType(new Integer32(messageDatabaseId));
		structure.addDataType(new Integer8(messageState));
		setValueAttr(structure);
    }    
    
    public int getMessageDatabaseId() throws IOException {
		return getValueAttr().getStructure().getDataType(0).intValue();
    }
    
    public String getContents() throws IOException {
    	if (getValueAttr().getStructure().nrOfDataTypes()==2)
    		return null;
    	else
    		return getValueAttr().getStructure().getDataType(1).isOctetString()?dataType.getStructure().getDataType(1).getOctetString().stringValue():null;
    }
    
    public Date getReleaseDate() throws IOException {
    	if (getValueAttr().getStructure().nrOfDataTypes()==2)
    		return null;
    	else
    		return new Date(getValueAttr().getStructure().getDataType(2).longValue()*1000);
    }
    
    public int getUserId() throws IOException {
    	if (getValueAttr().getStructure().nrOfDataTypes()==2)
    		return 0;
    	else
    		return getValueAttr().getStructure().getDataType(3).intValue();
    }
    
    public String getTrackingId() throws IOException {
    	if (getValueAttr().getStructure().nrOfDataTypes()==2)
    		return null;
    	else
    		return getValueAttr().getStructure().getDataType(4).isOctetString()?dataType.getStructure().getDataType(4).getOctetString().stringValue():null;
    }
    
    public int getMessageState() throws IOException {
    	if (getValueAttr().getStructure().nrOfDataTypes()==2)
    		return getValueAttr().getStructure().getDataType(1).intValue();
    	else
    		return getValueAttr().getStructure().getDataType(5).intValue();
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
