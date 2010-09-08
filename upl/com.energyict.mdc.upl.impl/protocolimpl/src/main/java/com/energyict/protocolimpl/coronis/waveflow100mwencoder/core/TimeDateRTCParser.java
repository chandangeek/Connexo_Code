package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.ProtocolUtils;

public class TimeDateRTCParser {

	static Calendar parse(byte[] data, TimeZone timeZone) throws IOException {
		Calendar calendar = Calendar.getInstance(timeZone);
		int offset=0;
		calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.getInt(data, offset++, 1));
		calendar.set(Calendar.MONTH,ProtocolUtils.getInt(data, offset++, 1)-1);
		calendar.set(Calendar.YEAR,ProtocolUtils.getInt(data, offset++, 1)+2000);
		calendar.set(Calendar.DAY_OF_WEEK,ProtocolUtils.getInt(data, offset++, 1)+1);
		calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data, offset++, 1));
		calendar.set(Calendar.MINUTE,ProtocolUtils.getInt(data, offset++, 1));
		calendar.set(Calendar.SECOND,ProtocolUtils.getInt(data, offset++, 1));
		calendar.set(Calendar.MILLISECOND,0);
		return calendar;
	}
	
	static byte[] prepare(Calendar calendar) {
		byte[] data = new byte[7];
		
		data[0] = (byte)calendar.get(Calendar.DAY_OF_MONTH);
		data[1] = (byte)(calendar.get(Calendar.MONTH)+1);
		data[2] = (byte)(calendar.get(Calendar.YEAR)-2000);
		data[3] = (byte)(calendar.get(Calendar.DAY_OF_WEEK)-1);
		data[4] = (byte)calendar.get(Calendar.HOUR_OF_DAY);
		data[5] = (byte)calendar.get(Calendar.MINUTE);
		data[6] = (byte)calendar.get(Calendar.SECOND);
	
		return data;
	}
	
	static int size() {
		return 7;
	}
}
