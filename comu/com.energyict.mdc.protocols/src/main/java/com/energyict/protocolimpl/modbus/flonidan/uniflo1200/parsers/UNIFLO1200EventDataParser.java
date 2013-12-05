/**
 * UNIFLO1200ProfileDataParser.java
 *
 * Created on 22-dec-2008, 16:07:03 by jme
 *
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.events.UNIFLO1200EventData;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.loadprofile.UNIFLO1200ProfileInfo;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;

import java.io.IOException;
import java.util.Date;

/**
 * @author jme
 *
 */
public class UNIFLO1200EventDataParser {

	private static final int DEBUG = 0;
	private UNIFLO1200EventData eventData;
	private Date time;
	private int logType;
	private boolean status;
	private byte[] rawData;
	private String description;
	private int eiserverEventCode;
	private boolean timeChanged;

	/*
	 * Constructors
	 */

	public UNIFLO1200EventDataParser(UNIFLO1200EventData eventData) {
		this.eventData = eventData;
	}

	/*
	 * Private getters, setters and methods
	 */

	private String getLogTypeName(int logType) {

		switch (logType) {
			case 0:  return "Tamper alarm";
			case 1:  return "Eprom error";
			case 2:  return "Pressure sensor error";
			case 3:  return "Temperature sensor error";
			case 4:  return "Pulse count error";
			case 5:  return "Pressure sensor error";
			case 6:  return "Door alarm";
			case 7:  return "Mains power error";
			case 8:  return "Temperature low limit";
			case 9:  return "Tempreature high limit";
			case 10: return "Pressure low limit";
			case 11: return "Pressure high limit";
			case 12: return "Flow high limit";
			case 13: return "Flow conv. high limit";
			case 14: return "Energy high";
			case 15: return "Pressure sensor temp error";
			case 16: return "Low battery warning";
			case 17: return "Conversion error";
			case 18: return "Program checksum error";
			case 19: return "A/D convertor error";
			case 20: return "Pressure range error";
			case 21: return "Extern alarm";
			case 22: return "Eprom 2 error";
			case 23: return "Option board alarm";
			case 24: return "Config. checksum error";
			case 25: return "Time adjusted more than 1% of log intv.";
			case 26: return "Power up";
			case 27: return "Event log is full";
			case 28: return "Conversion table transfer error";
			case 29: return "NC";
			case 30: return "NC";
			case 31: return "NC";
			case 32: return "NC";
			case 33: return "NC";
			case 34: return "NC";
			case 35: return "NC";
			case 36: return "NC";
			case 37: return "NC";
			case 38: return "NC";
			case 39: return "NC";
			case 40: return "Option board 1 alarm 1";
			case 41: return "Option board 1 alarm 2";
			case 42: return "Option board 1 alarm 3";
			case 43: return "Option board 1 alarm 4";
			case 44: return "Option board 1 alarm 5";
			case 45: return "Option board 1 alarm 6";
			case 46: return "Option board 1 alarm 7";
			case 47: return "Option board 1 alarm 8";
			case 48: return "Option board 2 alarm 1";
			case 49: return "Option board 2 alarm 2";
			case 50: return "Option board 2 alarm 3";
			case 51: return "Option board 2 alarm 4";
			case 52: return "Option board 2 alarm 5";
			case 53: return "Option board 2 alarm 6";
			case 54: return "Option board 2 alarm 7";
			case 55: return "Option board 2 alarm 8";
			case 56: return "Option board 3 alarm 1";
			case 57: return "Option board 3 alarm 2";
			case 58: return "Option board 3 alarm 3";
			case 59: return "Option board 3 alarm 4";
			case 60: return "Option board 3 alarm 5";
			case 61: return "Option board 3 alarm 6";
			case 62: return "Option board 3 alarm 7";
			case 63: return "Option board 3 alarm 8";
			case 64: return "Option board 4 alarm 1";
			case 65: return "Option board 4 alarm 2";
			case 66: return "Option board 4 alarm 3";
			case 67: return "Option board 4 alarm 4";
			case 68: return "Option board 4 alarm 5";
			case 69: return "Option board 4 alarm 6";
			case 70: return "Option board 4 alarm 7";
			case 71: return "Option board 4 alarm 8";
			case 72: return "Option board 5 alarm 1";
			case 73: return "Option board 5 alarm 2";
			case 74: return "Option board 5 alarm 3";
			case 75: return "Option board 5 alarm 4";
			case 76: return "Option board 5 alarm 5";
			case 77: return "Option board 5 alarm 6";
			case 78: return "Option board 5 alarm 7";
			case 79: return "Option board 5 alarm 8";
			case 80: return "Option board 6 alarm 1";
			case 81: return "Option board 6 alarm 2";
			case 82: return "Option board 6 alarm 3";
			case 83: return "Option board 6 alarm 4";
			case 84: return "Option board 6 alarm 5";
			case 85: return "Option board 6 alarm 6";
			case 86: return "Option board 6 alarm 7";
			case 87: return "Option board 6 alarm 8";
			case 88: return "Option board 7 alarm 1";
			case 89: return "Option board 7 alarm 2";
			case 90: return "Option board 7 alarm 3";
			case 91: return "Option board 7 alarm 4";
			case 92: return "Option board 7 alarm 5";
			case 93: return "Option board 7 alarm 6";
			case 94: return "Option board 7 alarm 7";
			case 95: return "Option board 7 alarm 8";

			default: return "Unknown alarm type: " + logType;

		}

	}

	private int getEisCode(int logType, boolean state) {

		switch (logType) {
			case 0:  return MeterEvent.OTHER; 									// Tamper alarm; //FIXME: Change event to TAMPER
			case 1:  return MeterEvent.ROM_MEMORY_ERROR; 						// Eprom error;
			case 2:  return MeterEvent.HARDWARE_ERROR; 							// Pressure sensor error;
			case 3:  return MeterEvent.HARDWARE_ERROR; 							// Temperature sensor error;
			case 4:  return MeterEvent.PROGRAM_FLOW_ERROR; 						// Pulse count error;
			case 5:  return MeterEvent.HARDWARE_ERROR; 							// Pressure sensor error;
			case 6:  return MeterEvent.OTHER; 									// Door alarm; //FIXME: Change event to DOOROPENED

			case 7:  															// Mains power error.
				if (state) return MeterEvent.POWERUP;							//    * State OFF  --> Power UP
					else return MeterEvent.POWERDOWN;							//    * State ON   --> Power DOWN

			case 8:  return MeterEvent.REGISTER_OVERFLOW;						// Temperature low limit;
			case 9:  return MeterEvent.REGISTER_OVERFLOW;						// Temperature high limit;
			case 10: return MeterEvent.REGISTER_OVERFLOW; 						// Pressure low limit;
			case 11: return MeterEvent.REGISTER_OVERFLOW; 						// Pressure high limit;
			case 12: return MeterEvent.REGISTER_OVERFLOW; 						// Flow high limit;
			case 13: return MeterEvent.REGISTER_OVERFLOW; 						// Flow conv. high limit;
			case 14: return MeterEvent.REGISTER_OVERFLOW; 						// Energy high;
			case 15: return MeterEvent.HARDWARE_ERROR; 							// Pressure sensor temp error;
			case 16: return MeterEvent.METER_ALARM; 							// Low battery warning;
			case 17: return MeterEvent.PROGRAM_FLOW_ERROR;						// Conversion error;
			case 18: return MeterEvent.PROGRAM_FLOW_ERROR; 						// Program checksum error;
			case 19: return MeterEvent.HARDWARE_ERROR; 							// A/D convertor error;
			case 20: return MeterEvent.OTHER; 									// Pressure range error;
			case 21: return MeterEvent.METER_ALARM; 							// Extern alarm;
			case 22: return MeterEvent.ROM_MEMORY_ERROR;						// Eprom 2 error;
			case 23: return MeterEvent.OTHER; 									// Option board alarm;
			case 24: return MeterEvent.OTHER; 									// Config. checksum error;
			case 25: return MeterEvent.SETCLOCK;								// Time adjusted more than 1% of log intv.;
			case 26: return MeterEvent.POWERUP; 								// Power up;
			case 27: return MeterEvent.PROGRAM_FLOW_ERROR;						// Event log is full;
			case 28: return MeterEvent.PROGRAM_FLOW_ERROR; 						// Conversion table transfer error;
			case 29: return MeterEvent.OTHER; 									// NC;
			case 30: return MeterEvent.OTHER; 									// NC;
			case 31: return MeterEvent.OTHER; 									// NC;
			case 32: return MeterEvent.OTHER; 									// NC;
			case 33: return MeterEvent.OTHER; 									// NC;
			case 34: return MeterEvent.OTHER; 									// NC;
			case 35: return MeterEvent.OTHER; 									// NC;
			case 36: return MeterEvent.OTHER; 									// NC;
			case 37: return MeterEvent.OTHER; 									// NC;
			case 38: return MeterEvent.OTHER; 									// NC;
			case 39: return MeterEvent.OTHER; 									// NC;
			case 40: return MeterEvent.METER_ALARM; 							// Option board 1 alarm 1;
			case 41: return MeterEvent.METER_ALARM; 							// Option board 1 alarm 2;
			case 42: return MeterEvent.METER_ALARM; 							// Option board 1 alarm 3;
			case 43: return MeterEvent.METER_ALARM; 							// Option board 1 alarm 4;
			case 44: return MeterEvent.METER_ALARM; 							// Option board 1 alarm 5;
			case 45: return MeterEvent.METER_ALARM; 							// Option board 1 alarm 6;
			case 46: return MeterEvent.METER_ALARM; 							// Option board 1 alarm 7;
			case 47: return MeterEvent.METER_ALARM; 							// Option board 1 alarm 8;
			case 48: return MeterEvent.METER_ALARM; 							// Option board 2 alarm 1;
			case 49: return MeterEvent.METER_ALARM; 							// Option board 2 alarm 2;
			case 50: return MeterEvent.METER_ALARM; 							// Option board 2 alarm 3;
			case 51: return MeterEvent.METER_ALARM; 							// Option board 2 alarm 4;
			case 52: return MeterEvent.METER_ALARM; 							// Option board 2 alarm 5;
			case 53: return MeterEvent.METER_ALARM; 							// Option board 2 alarm 6;
			case 54: return MeterEvent.METER_ALARM; 							// Option board 2 alarm 7;
			case 55: return MeterEvent.METER_ALARM; 							// Option board 2 alarm 8;
			case 56: return MeterEvent.METER_ALARM; 							// Option board 3 alarm 1;
			case 57: return MeterEvent.METER_ALARM; 							// Option board 3 alarm 2;
			case 58: return MeterEvent.METER_ALARM; 							// Option board 3 alarm 3;
			case 59: return MeterEvent.METER_ALARM; 							// Option board 3 alarm 4;
			case 60: return MeterEvent.METER_ALARM; 							// Option board 3 alarm 5;
			case 61: return MeterEvent.METER_ALARM; 							// Option board 3 alarm 6;
			case 62: return MeterEvent.METER_ALARM; 							// Option board 3 alarm 7;
			case 63: return MeterEvent.METER_ALARM; 							// Option board 3 alarm 8;
			case 64: return MeterEvent.METER_ALARM; 							// Option board 4 alarm 1;
			case 65: return MeterEvent.METER_ALARM; 							// Option board 4 alarm 2;
			case 66: return MeterEvent.METER_ALARM; 							// Option board 4 alarm 3;
			case 67: return MeterEvent.METER_ALARM; 							// Option board 4 alarm 4;
			case 68: return MeterEvent.METER_ALARM; 							// Option board 4 alarm 5;
			case 69: return MeterEvent.METER_ALARM; 							// Option board 4 alarm 6;
			case 70: return MeterEvent.METER_ALARM; 							// Option board 4 alarm 7;
			case 71: return MeterEvent.METER_ALARM; 							// Option board 4 alarm 8;
			case 72: return MeterEvent.METER_ALARM; 							// Option board 5 alarm 1;
			case 73: return MeterEvent.METER_ALARM; 							// Option board 5 alarm 2;
			case 74: return MeterEvent.METER_ALARM; 							// Option board 5 alarm 3;
			case 75: return MeterEvent.METER_ALARM; 							// Option board 5 alarm 4;
			case 76: return MeterEvent.METER_ALARM; 							// Option board 5 alarm 5;
			case 77: return MeterEvent.METER_ALARM; 							// Option board 5 alarm 6;
			case 78: return MeterEvent.METER_ALARM; 							// Option board 5 alarm 7;
			case 79: return MeterEvent.METER_ALARM; 							// Option board 5 alarm 8;
			case 80: return MeterEvent.METER_ALARM; 							// Option board 6 alarm 1;
			case 81: return MeterEvent.METER_ALARM; 							// Option board 6 alarm 2;
			case 82: return MeterEvent.METER_ALARM; 							// Option board 6 alarm 3;
			case 83: return MeterEvent.METER_ALARM; 							// Option board 6 alarm 4;
			case 84: return MeterEvent.METER_ALARM; 							// Option board 6 alarm 5;
			case 85: return MeterEvent.METER_ALARM; 							// Option board 6 alarm 6;
			case 86: return MeterEvent.METER_ALARM; 							// Option board 6 alarm 7;
			case 87: return MeterEvent.METER_ALARM; 							// Option board 6 alarm 8;
			case 88: return MeterEvent.METER_ALARM; 							// Option board 7 alarm 1;
			case 89: return MeterEvent.METER_ALARM; 							// Option board 7 alarm 2;
			case 90: return MeterEvent.METER_ALARM; 							// Option board 7 alarm 3;
			case 91: return MeterEvent.METER_ALARM; 							// Option board 7 alarm 4;
			case 92: return MeterEvent.METER_ALARM; 							// Option board 7 alarm 5;
			case 93: return MeterEvent.METER_ALARM; 							// Option board 7 alarm 6;
			case 94: return MeterEvent.METER_ALARM; 							// Option board 7 alarm 7;
			case 95: return MeterEvent.METER_ALARM; 							// Option board 7 alarm 8;
			default: return MeterEvent.OTHER; 									// Unknown alarm type:  + logType;

		}

	}

	private UNIFLO1200EventData getEventData() {
		return eventData;
	}

	private UNIFLO1200ProfileInfo getProfileInfo() {
		return getEventData().getLoadProfile().getProfileInfo();
	}

	private Parser getParser(String parserName) throws IOException {
		return getRegisterFactory().getParserFactory().get(parserName);
	}

	private UNIFLO1200RegisterFactory getRegisterFactory() {
		return (UNIFLO1200RegisterFactory) getUniflo1200().getRegisterFactory();
	}

	private UNIFLO1200 getUniflo1200() {
		return getEventData().getLoadProfile().getUniflo1200();
	}

	private int[] parseByteArray2IntArray(byte[] rawData) {
		int[] returnValue = new int[rawData.length / 2];
		for (int i = 0; i < returnValue.length; i++) {
			returnValue[i] = (rawData[i*2] & 0x000000FF) << 8;
			returnValue[i] += rawData[(i*2) + 1] & 0x000000FF;
		}
		return returnValue;
	}

	/*
	 * Public methods
	 */

	public void parseData(byte[] rawData) throws IOException {
		//List registers = getProfileInfo().getChannelRegisters();
		int[] intData = parseByteArray2IntArray(rawData);

		this.rawData = rawData;
		this.time = (Date) getParser(UNIFLO1200Parsers.PARSER_TIME).val(intData, null);
		this.logType = ((int)rawData[6]) & 0x000000FF;
		this.status = (rawData[8] != 0);

		this.description = getLogTypeName(getLogType()) + " Alarm: " + ((getStatus() == true)?"ON":"OFF");
		this.eiserverEventCode = getEisCode(getLogType(), getStatus());

		this.timeChanged = (getLogType() == 25);

	}

	/*
	 * Public getters and setters
	 */

	public Date getTime() {
		return this.time;
	}

	public int getLogType() {
		return logType;
	}

	public boolean getStatus() {
		return status;
	}

	public byte[] getRawData() {
		return rawData;
	}

	public String getDescription() {
		return description;
	}

	public int getEiserverEventCode() {
		return eiserverEventCode;
	}

	public boolean isTimeChanged() {
		return timeChanged;
	}

}
