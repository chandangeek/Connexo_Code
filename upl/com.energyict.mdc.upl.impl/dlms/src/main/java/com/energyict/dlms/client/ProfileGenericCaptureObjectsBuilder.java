package com.energyict.dlms.client;


import com.energyict.dlms.axrdencoding.*;

public class ProfileGenericCaptureObjectsBuilder {
	
	Array captureObjectsArray=null;
	
	public ProfileGenericCaptureObjectsBuilder() {
		reset();
	}
	
	public void reset() {
		captureObjectsArray=null;
	}
	
	public void add(int classId, byte[] logicalName,int attributeIndex) {
		if (captureObjectsArray == null)
			captureObjectsArray = new Array();
		Structure structure = new Structure();
		structure.addDataType(new Unsigned16(classId));
		structure.addDataType(new OctetString(logicalName));
		structure.addDataType(new Integer8(attributeIndex));
		captureObjectsArray.addDataType(structure);
	}

	public Array getCaptureObjectsArray() {
		return captureObjectsArray;
	}
	
}
