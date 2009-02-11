package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.MeterInfo;

public class MeterInfoParser implements Parser {

	private static final int DEBUG = 0;
	
	private static final int SERIAL_OFFSET	= 43;
	private static final int SERIAL_LENGTH	= 8;
	private static final int TIME_OFFSET 	= 30;
	private static final int TIME_LENGTH 	= 4;
	
	private TimeZone timeZone 		= null;

	/*
	 * Constructors
	 */

	public MeterInfoParser(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	
	/*
	 * Private getters, setters and methods
	 */

    private static String bytesToString(byte[] rawData) {
        String returnValue= ""; 
        for (int i = 0; i < rawData.length; i++) {
            returnValue += String.valueOf((char)rawData[i]);
		}
        return returnValue;
     }
	
    private String parseSerialNumber(byte[] rawData) {
    	return bytesToString(ProtocolUtils.getSubArray2(rawData, SERIAL_OFFSET, SERIAL_LENGTH));
    }
    
	private Date parseTime(byte[] rawData) {
		Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
		long secondsSince1970GMT = 0;

		for (int i = 0; i < TIME_LENGTH; i++) {
			long value = ((long)rawData[TIME_OFFSET+i]) & 0x00FF; 
			secondsSince1970GMT += value << ((TIME_LENGTH - (i+1)) * 8);
		}
		
		cal.setTime(new Date(secondsSince1970GMT * 1000));
		cal.add(Calendar.HOUR, -1);
		return cal.getTime();
	}

	private String parseVersion(byte[] rawData) {
		return "AANPASSEN";
	}

	/*
	 * Public methods
	 */

	public MeterInfo val(int[] values, AbstractRegister register) throws IOException {
		String serialNumber;
		Date time;
		String version;
		byte[] rawData;
		
		rawData = new byte[values.length * 2];
		for (int i = 0; i < values.length; i++) {
			rawData[i*2] 		= (byte) ((values[i] & 0x0000FF00) >> 8);
			rawData[(i*2) + 1] 	= (byte) (values[i] & 0x000000FF);
		}
		
		serialNumber = parseSerialNumber(rawData);
		version = parseVersion(rawData);
		time = parseTime(rawData);
		
		return new MeterInfo(serialNumber, time, version, rawData);
	}

	/*
	 * Public getters and setters
	 */

}
