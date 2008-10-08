package com.energyict.dlms.axrdencoding.util;

import java.util.Date;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.*;

public class AXDRDate {

	
	static public Unsigned32 encode(Date date) {
		return new Unsigned32(date.getTime()/1000);
	}
	
	static public Date decode(AbstractDataType dataType) {
		Date date = new Date(dataType.longValue()*1000);
		return date;
	}	
	
	static public void main(String[] args) {
		Date date = new Date();
		Unsigned32 o = encode(date);
		System.out.println(o);
		System.out.println(decode(o));
	}
	
}
