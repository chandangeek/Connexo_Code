package com.energyict.dlms.client;

import java.io.IOException;
import java.util.*;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class ProfileGenericBufferBuilder {
	
	Array bufferArray=null;
	Structure bufferStructure;
	
	public ProfileGenericBufferBuilder() {
		reset();
	}

	public void reset() {
		bufferArray=null;
	}
	
	public void addRegisterValue(RegisterValue registerValue) throws IOException {
		if (bufferStructure==null)
			bufferStructure = new Structure();
		

		Structure structure = new Structure();
	/*
	    ObisCode obisCode; // as support for the toString()
	    int rtuRegisterId; // to find back the RtuRegister to which this RegisterValue belongs
	    Quantity quantity;
	    Date readTime;
	    Date fromTime;
	    Date toTime; // billing timestamp
	    Date eventTime; // Maximum demand timestamp
	    String text;		
	*/
		// logical name
		structure.addDataType(new OctetString(registerValue.getObisCode().getLN()));
		// value
		Structure entry = new Structure();
		entry.addDataType(new Unsigned32(registerValue.getRtuRegisterId()));
		entry.addDataType(new NumberFormat(registerValue.getQuantity().getAmount()).toAbstractDataType());
		entry.addDataType(new ScalerUnit(registerValue.getQuantity().getUnit()).getAbstractDataType());
		entry.addDataType(AXDRDate.encode(registerValue.getReadTime()));
		entry.addDataType(AXDRDate.encode(registerValue.getFromTime()));
		entry.addDataType(AXDRDate.encode(registerValue.getToTime()));
		entry.addDataType(AXDRDate.encode(registerValue.getEventTime()));
		entry.addDataType(OctetString.fromString(registerValue.getText()));
		structure.addDataType(entry);
		
		bufferStructure.addDataType(structure);
	}
	
	public void addAbstractDataType(AbstractDataType dataType) throws IOException {
		if (bufferArray==null)
			bufferArray = new Array();
		bufferArray.addDataType(dataType);
	}
	
	public void addMeterEvent(MeterEvent meterEvent) throws IOException {
		if (bufferArray==null)
			bufferArray = new Array();

		Structure structure = new Structure();
		DateTime dateTime = new DateTime(meterEvent.getTime());
		structure.addDataType(new OctetString(dateTime.getBEREncodedByteArray(),0));
		Structure eventStructure = new Structure();
		eventStructure.addDataType(new Unsigned16(meterEvent.getEiCode()));
		eventStructure.addDataType(new Unsigned32(meterEvent.getProtocolCode()));
		eventStructure.addDataType(OctetString.fromString(meterEvent.getMessage()));
		structure.addDataType(eventStructure);
		bufferArray.addDataType(structure);
		
	}

	public void addInterval(IntervalData intervalData,Map<Integer,ObisCode> channelStatusFlags) throws IOException {
		addInterval(intervalData.getEiStatus(),intervalData.getIntervalValues(),intervalData.getEndTime(),channelStatusFlags);
	}
	
	private void addInterval(int status, List<IntervalValue> values, Date date,Map<Integer,ObisCode> channelStatusFlags) throws IOException {
		if (bufferArray==null)
			bufferArray = new Array();
		Structure structure = new Structure();
		
//		DateTime dateTime = new DateTime(date);
//		structure.addDataType(new OctetString(dateTime.getBEREncodedByteArray(),0));
		
		structure.addDataType(new Unsigned32(date.getTime()/1000)); // Clock 0.0.96.101.0.0 seconds since 1/1/1970 GMT
		
		structure.addDataType(new Unsigned32(status));
		for (int i=0;i<values.size();i++) {
			
			if (channelStatusFlags.get(Integer.valueOf(i))!=null)
				structure.addDataType(new Unsigned32(values.get(i).getEiStatus()));
			
			Number value = values.get(i).getNumber();
			NumberFormat nfb = new NumberFormat(value);
			structure.addDataType(nfb.toAbstractDataType());
		}
		bufferArray.addDataType(structure);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public Array getBufferArray() {
		if ((bufferStructure != null) && (bufferArray==null)) {
			bufferArray = new Array();
			bufferArray.addDataType(bufferStructure);
		}
		return bufferArray;
	}

}
