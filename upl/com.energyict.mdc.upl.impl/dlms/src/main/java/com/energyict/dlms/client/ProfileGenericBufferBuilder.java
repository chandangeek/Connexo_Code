package com.energyict.dlms.client;

import java.io.IOException;
import java.util.*;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class ProfileGenericBufferBuilder {
	
	Array bufferArray=null;
	
	public ProfileGenericBufferBuilder() {
		reset();
	}

	public void reset() {
		bufferArray=null;
	}
	
	public void addRegisterValue(RegisterValue registerValue) throws IOException {
		if (bufferArray==null)
			bufferArray = new Array();
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
		entry.addDataType(new DateTime(registerValue.getReadTime()));
		entry.addDataType(new DateTime(registerValue.getFromTime()));
		entry.addDataType(new DateTime(registerValue.getToTime()));
		entry.addDataType(new DateTime(registerValue.getEventTime()));
		entry.addDataType(OctetString.fromString(registerValue.getText()));
		structure.addDataType(entry);
		
		bufferArray.addDataType(structure);
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

	public void addInterval(IntervalData intervalData) throws IOException {
		List<Number> values = new ArrayList();
		for (int i=0;i<intervalData.getIntervalValues().size();i++)
		   values.add(intervalData.get(i));
		addInterval(intervalData.getEiStatus(),values,intervalData.getEndTime());
	}
	
	private void addInterval(int status, List<Number> values, Date date) throws IOException {
		if (bufferArray==null)
			bufferArray = new Array();
		Structure structure = new Structure();
		DateTime dateTime = new DateTime(date);
		structure.addDataType(new OctetString(dateTime.getBEREncodedByteArray(),0));
		structure.addDataType(new Unsigned32(status));
		for (int i=0;i<values.size();i++) {
			Number value = values.get(i);
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
		return bufferArray;
	}

}
