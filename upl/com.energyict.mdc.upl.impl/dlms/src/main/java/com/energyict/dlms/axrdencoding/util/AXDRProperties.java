package com.energyict.dlms.axrdencoding.util;

import java.io.IOException;
import java.util.*;

import com.energyict.dlms.axrdencoding.*;

public class AXDRProperties {

	static public Array encode(Properties properties) throws IOException {
		Array array = new Array();
		//Structure structure = new Structure();
		
		Enumeration e = properties.keys();
		while(e.hasMoreElements()) {
			//String key = (String)e.nextElement();
			Object key = e.nextElement();
			String value = (String)properties.get(key);
			Structure keyValue = new Structure();
			
			if (key instanceof Integer)
				keyValue.addDataType(new Integer32(((Integer)key).intValue()));
			else if (key instanceof String)
				keyValue.addDataType(OctetString.fromString((String)key));
			else throw new IOException("Invalid key type for "+key.getClass().getName());
			
			keyValue.addDataType(OctetString.fromString(value));
			array.addDataType(keyValue);
		}
		
		return array;
	}
	
	static public Properties decode(AbstractDataType dataType) {
		Properties properties = new Properties();
		
		//Structure structure = dataType.getStructure();
		Array array = dataType.getArray();
		for (int i=0;i<array.nrOfDataTypes();i++) {
			Structure keyValue = array.getDataType(i).getStructure();
			Object key=null;
			if (keyValue.getDataType(0).isOctetString())
				key = keyValue.getDataType(0).getOctetString().stringValue();
			else if (keyValue.getDataType(0).isInteger32())
				key = new Integer(keyValue.getDataType(0).intValue());
			
			String value = keyValue.getDataType(1).getOctetString().stringValue();
			properties.put(key,value);
		}
		
		
		return properties;
	}	
	
	static public void main(String[] args) {
		Properties properties = new Properties();
		properties.put(new Integer(2), "1000");
		properties.put("test2", "1234");
		properties.put("test3", "");
		properties.put("test4", "testvalue");
		
		try {
			Array array = encode(properties);
			System.out.println(array);
			System.out.println(decode(array));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
