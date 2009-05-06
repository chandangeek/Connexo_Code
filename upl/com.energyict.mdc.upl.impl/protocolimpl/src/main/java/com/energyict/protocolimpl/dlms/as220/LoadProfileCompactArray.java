package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.*;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocol.ProtocolUtils;

public class LoadProfileCompactArray {

	final int DEBUG=1;
	
	List loadProfileCompactArrayEntries = new ArrayList();
	TimeZone timeZone;
	
	public LoadProfileCompactArray(TimeZone timeZone) {
		this.timeZone=timeZone;
	}
	
	public void  parse(byte[] data) throws IOException {
		int offset = 0;
		
		if (data[offset] != 19)
			throw new IOException("No compact array!");
		
		offset+=4; // skip compact array tag AND TypeDescription
		
        int length = (int)DLMSUtils.getAXDRLength(data,offset);
        offset += DLMSUtils.getAXDRLengthOffset(data,offset); 
        
        if ((length % 4) != 0)
        	throw new IOException("Not an integer number of unsigned32 data values in the compact array!");
		
        if (DEBUG>=1) System.out.println("LoadProfileCompactArray, parse "+(length/4)+" values"); 
        
        for (int i=0;i<length/4;i++) {
        	Long value = new Long(ProtocolUtils.getLong(data, offset, 4));
        	offset+=4;
        	loadProfileCompactArrayEntries.add(new LoadProfileCompactArrayEntry(value,timeZone));
        }
        
	}

	public List getLoadProfileCompactArrayEntries() {
		return loadProfileCompactArrayEntries;
	}
	
}
