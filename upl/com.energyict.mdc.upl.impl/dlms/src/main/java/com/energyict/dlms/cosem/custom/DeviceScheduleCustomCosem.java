package com.energyict.dlms.cosem.custom;

import java.io.IOException;
import java.util.Date;
import java.util.regex.*;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.client.CompoundDataBuilderConnection;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

public class DeviceScheduleCustomCosem extends Data {

	AbstractDataType dataType=null;
	
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DeviceScheduleCustomCosem:\n");
		strBuff.append("   scheduleId="+getScheduleId()+"\n");
        strBuff.append("   nextCommunication="+getNextCommunication()+"\n");
        return strBuff.toString();
    }	
	
	static final byte[] LN=new byte[]{0,0,96,112,0,0};
	
	public DeviceScheduleCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public DeviceScheduleCustomCosem() {
		super(new CompoundDataBuilderConnection(),new ObjectReference(LN));
	}
	
	public DeviceScheduleCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(int scheduleId, Date nextCommunication) throws IOException {
		Structure structure = new Structure();
		structure.addDataType(new Integer32(scheduleId));
		structure.addDataType(AXDRDate.encode(nextCommunication));
		setValueAttr(structure);
    }
    

    public int getScheduleId() {
    	return getValueAttr().getStructure().getDataType(0).intValue();
    }
    
    public Date getNextCommunication() {
    	return AXDRDate.decode(getValueAttr().getStructure().getDataType(1));
    }
    
    public AbstractDataType getValueAttr() {
    	return dataType;
    }
    
    public void setValueAttr(AbstractDataType val) throws IOException {
    	dataType = val;
    	super.setValueAttr(dataType);
    }

}
