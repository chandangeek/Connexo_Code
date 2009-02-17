package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.MeterInfo;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;

public class MeterInfoParser implements Parser {

	private static final int DEBUG = 0;
	
	private static final int SERIAL_OFFSET	= 42;
	private static final int SERIAL_LENGTH	= 10;
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
    
	private String parseVersion(byte[] rawData) {
		return "AANPASSEN";
	}

	/*
	 * Public methods
	 */

	public MeterInfo val(int[] values, AbstractRegister register) throws IOException {
		byte[] rawData = Utils.intArrayToByteArray(values);
		TimeDateParser td_parser = new TimeDateParser(timeZone);
		
		String serialNumber = parseSerialNumber(rawData);
		String version = parseVersion(rawData);
		Date time = td_parser.parseTime(ProtocolUtils.getSubArray2(rawData, TIME_OFFSET, TIME_LENGTH));
		
		if (serialNumber != null) serialNumber = serialNumber.trim(); 
		
		return new MeterInfo(serialNumber, time, version, rawData);
	}

	/*
	 * Public getters and setters
	 */

}
