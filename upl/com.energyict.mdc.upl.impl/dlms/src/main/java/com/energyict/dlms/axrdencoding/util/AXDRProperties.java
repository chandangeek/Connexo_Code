package com.energyict.dlms.axrdencoding.util;

import java.util.*;

import com.energyict.dlms.axrdencoding.*;

public class AXDRProperties {

	static public Structure encode(Properties properties) {
		Structure structure = new Structure();
		
		Enumeration e = properties.keys();
		while(e.hasMoreElements()) {
			String key = (String)e.nextElement();
			String value = properties.getProperty(key);
			Structure keyValue = new Structure();
			keyValue.addDataType(OctetString.fromString(key));
			keyValue.addDataType(OctetString.fromString(value));
			structure.addDataType(keyValue);
		}
		
		return structure;
	}
	
	static public Properties decode(AbstractDataType dataType) {
		Properties properties = new Properties();
		
		Structure structure = dataType.getStructure();
		for (int i=0;i<structure.nrOfDataTypes();i++) {
			
			Structure keyValue = structure.getDataType(i).getStructure();
			String key = keyValue.getDataType(0).getOctetString().stringValue();
			String value = keyValue.getDataType(1).getOctetString().stringValue();
			properties.put(key,value);
		}
		
		
		return properties;
	}	
	
	static public void main(String[] args) {
		Properties properties = new Properties();
		properties.put("test1", "1000");
		properties.put("test2", "1234");
		properties.put("test3", "");
		properties.put("test4", "testvalue");
		
		Structure structure = encode(properties);
		System.out.println(structure);
		
		System.out.println(decode(structure));
		
		
	}
}
