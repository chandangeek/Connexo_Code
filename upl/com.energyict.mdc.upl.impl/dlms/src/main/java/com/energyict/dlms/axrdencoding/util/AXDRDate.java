package com.energyict.dlms.axrdencoding.util;

import java.util.Date;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.*;

public class AXDRDate {

	
	static public AbstractDataType encode(Date date) {
		if (date==null)
			return new NullData();
		else
			return new Unsigned32(date.getTime()/1000);
	}
	
	static public Date decode(AbstractDataType dataType) {
		if (dataType.isNullData()) 
			return null;
		else
			return new Date(dataType.longValue()*1000);
	}	
	
	static public void main(String[] args) {
		Date date = null; //new Date();
		if (!encode(date).isNullData()) {
			Unsigned32 o = (Unsigned32)encode(date);
			System.out.println(o);
			System.out.println(decode(o));
		}
		else System.out.println("null!");
	}
	
}
