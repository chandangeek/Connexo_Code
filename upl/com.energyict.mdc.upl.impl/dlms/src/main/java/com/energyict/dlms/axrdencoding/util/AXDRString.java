package com.energyict.dlms.axrdencoding.util;

import java.util.Date;

import com.energyict.dlms.axrdencoding.*;

public class AXDRString {

	
	static public AbstractDataType encode(String string) {
		if (string==null)
			return new NullData();
		else
			return OctetString.fromString(string);
	}
	
	static public String decode(AbstractDataType dataType) {
		if (dataType.isNullData())
			return null;
		else
			return dataType.getOctetString().stringValue();
	}	
	
	static public void main(String[] args) {

	}
	
}
