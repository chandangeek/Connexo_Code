package com.energyict.dlms.cosem.custom;

import java.io.IOException;
import java.util.*;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.dlms.client.CompoundDataBuilderConnection;
import com.energyict.dlms.cosem.*;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.obis.ObisCode;

public class TaskStatusCustomCosem extends Data {

	AbstractDataType dataType=null;
	
    public static final int TASK_RESULT_TOEVALUATE=2;
    public static final int TASK_RESULT_FAIL=0;
    public static final int TASK_RESULT_SUCCESS=1;
    public static final String[] strResults={"RESULT_FAIL","RESULT_SUCCESS","RESULT_TOEVALUATE"};
	
	static final byte[] LN=new byte[]{0,0,96,71,0,0};
	
	public TaskStatusCustomCosem(AbstractDataType dataType) {
		super(null,new ObjectReference(LN));
		this.dataType=dataType;
	}
	
	public TaskStatusCustomCosem() {
		super(new CompoundDataBuilderConnection(),new ObjectReference(LN));
	}
	
	public TaskStatusCustomCosem(ProtocolLink protocolLink) {
		super(protocolLink,new ObjectReference(LN));
    }
    
	static public ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}
	
    protected int getClassId() {
        return AbstractCosemObject.CLASSID_DATA;
    }

    public void setFields(int status,List<AmrJournalEntry> amrJournalEntries) throws IOException {
		Structure structure = new Structure();
		structure.addDataType(new Integer8(status));
		Array amrEntries = new Array();
		for (int i=0;i<amrJournalEntries.size();i++) {
			Structure amrEntry = new Structure();
			amrEntry.addDataType(AXDRDate.encode(amrJournalEntries.get(i).getDate()));
			amrEntry.addDataType(new Integer8(amrJournalEntries.get(i).getCode()));
			amrEntry.addDataType(AXDRString.encode(amrJournalEntries.get(i).getComPortName()));
			amrEntry.addDataType(AXDRString.encode(amrJournalEntries.get(i).getInfo()));
			amrEntries.addDataType(amrEntry);
		}
		structure.addDataType(amrEntries);
		setValueAttr(structure);
    }
    
    public int getStatus() { // throws IOException {
		return getValueAttr().getStructure().getDataType(0).intValue();
    }
    
    public List<AmrJournalEntry> getAmrJournalEntries() { // throws IOException {
    	List<AmrJournalEntry> amrJournalEntries = new ArrayList<AmrJournalEntry>();
    	Array amrEntries = getValueAttr().getStructure().getDataType(1).getArray();
    	for (int i=0;i<amrEntries.nrOfDataTypes();i++) {
    		Structure amrEntry = amrEntries.getDataType(i).getStructure();
    		Date date = AXDRDate.decode(amrEntry.getNextDataType());
    		int code = amrEntry.getNextDataType().intValue();
    		String comPortName = AXDRString.decode(amrEntry.getNextDataType());
    		String info = AXDRString.decode(amrEntry.getNextDataType());
    		amrJournalEntries.add(new AmrJournalEntry(date,comPortName,code,info));
    	}
    	return amrJournalEntries;
    }

    public AbstractDataType getValueAttr() { //throws IOException {
//    	if (dataType == null)
//    		dataType = super.getValueAttr();
    	return dataType;
    }
    
    public void setValueAttr(AbstractDataType val) throws IOException {
    	dataType = val;
    	super.setValueAttr(dataType);
    }
    
}
