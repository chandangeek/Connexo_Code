package com.energyict.dlms.axrdencoding.util;

import java.util.Date;

import com.energyict.dlms.axrdencoding.*;

public class AXDRBoolean {

	
	static public Integer8 encode(boolean val) {
		return new Integer8(val?1:0);
	}
	
	static public boolean decode(AbstractDataType dataType) {
		return dataType.intValue()==1?true:false;
	}	
	
	static public void main(String[] args) {
		boolean val = false;
		Integer8 o = encode(val);
		System.out.println(o);
		System.out.println(decode(o));
	}
	
}
