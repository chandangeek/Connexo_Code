package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.MeterInfo;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

public class MeterInfoParser implements Parser {

	private static final int DEBUG = 0;
	
	private static final int SERIAL_OFFSET	= 42;
	private static final int SERIAL_LENGTH	= 10;
	private static final int VERSION_OFFSET	= 20;
	private static final int VERSION_LENGTH	= 2;
	private static final int TIME_OFFSET 	= 30;
	private static final int TIME_LENGTH 	= 4;
	
	public static final String PARSER_NAME = "MeterInfoParser";
	
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
		String returnValue = "";
		byte[] versionRaw = ProtocolUtils.getSubArray2(rawData, VERSION_OFFSET, VERSION_LENGTH);
		int major = ((int)versionRaw[0]) & 0x000000FF;
		int minor = ((int)versionRaw[1]) & 0x000000FF;
        returnValue = "Main board: " + major + "." + (minor < 10 ? "0" + minor : minor);
		return returnValue;
	}

	/*
	 * Public methods
	 */

	public Object val(int[] values, AbstractRegister register) throws IOException {
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
