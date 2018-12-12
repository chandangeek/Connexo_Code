package com.energyict.protocolimpl.modbus.enerdis.enerium200.core;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {

	private static final int DEBUG 				= 0;
	private static final int MODBUS_MAX_SIZE 	= 0x078;

	public static final int SETCLOCK			= 0x0104;
	public static final int SETPROFILEPART 		= 0x0705;

	public static byte[] intArrayToByteArray(int[] intArray) {
		byte[] byteArray = new byte[intArray.length*2];
		for (int i = 0; i < intArray.length; i++) {
			byteArray[i*2] 			= (byte) ((intArray[i] & 0x0000FF00) >> 8);
			byteArray[(i*2) + 1] 	= (byte) (intArray[i] & 0x000000FF);
		}
		return byteArray;
	}

	public static byte[] longToBytes(long value) {
		byte[] returnValue = new byte[8];
		returnValue[7] = (byte) ((value >> 0)  & 0x00000000000000FFL);
		returnValue[6] = (byte) ((value >> 8)  & 0x00000000000000FFL);
		returnValue[5] = (byte) ((value >> 16) & 0x00000000000000FFL);
		returnValue[4] = (byte) ((value >> 24) & 0x00000000000000FFL);
		returnValue[3] = (byte) ((value >> 32) & 0x00000000000000FFL);
		returnValue[2] = (byte) ((value >> 40) & 0x00000000000000FFL);
		returnValue[1] = (byte) ((value >> 48) & 0x00000000000000FFL);
		returnValue[0] = (byte) ((value >> 56) & 0x00000000000000FFL);
		return returnValue;
	}

	public static byte[] intToBytes(int value) {
		byte[] returnValue = new byte[4];
		returnValue[3] = (byte) ((value >> 0)  & 0x00000000000000FFL);
		returnValue[2] = (byte) ((value >> 8)  & 0x00000000000000FFL);
		returnValue[1] = (byte) ((value >> 16) & 0x00000000000000FFL);
		returnValue[0] = (byte) ((value >> 24) & 0x00000000000000FFL);
		return returnValue;
	}

	public static byte[] shortToBytes(short value) {
		byte[] returnValue = new byte[2];
		returnValue[1] = (byte) ((value >> 0)  & 0x00000000000000FFL);
		returnValue[0] = (byte) ((value >> 8)  & 0x00000000000000FFL);
		return returnValue;
	}

    private static int[] readRawIntValues(int address, int length, Modbus modbus)  throws IOException {
        HoldingRegister r = new HoldingRegister(address, length);
        r.setRegisterFactory(modbus.getRegisterFactory());
        return r.getReadHoldingRegistersRequest().getRegisters();
    }

    private static byte[] readRawByteValues(int address, int length, Modbus modbus)  throws IOException {
        return intArrayToByteArray(readRawIntValues(address, length, modbus));
    }

    private static void writeRawByteValues(int address, byte[] rawData, Modbus modbus)  throws IOException {
        HoldingRegister r = new HoldingRegister(address, rawData.length / 2);
        r.setRegisterFactory(modbus.getRegisterFactory());
        r.getWriteMultipleRegisters(rawData);
    }

    public static void writeRawByteValues(int address, int functionCode, byte[] rawData, Modbus modbus)  throws IOException {
    	byte[] functionBytes = shortToBytes((short) (functionCode & 0x0000FFFF));
    	writeRawByteValues(address, ProtocolUtils.concatByteArrays(functionBytes, rawData), modbus);
    }

    public static byte[] readByteValues(int address, int length, Modbus modbus)  throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int currentPos = 0;

		while (currentPos < length) {
			int currentLength = length - currentPos;
			if (currentLength > MODBUS_MAX_SIZE) {
				currentLength = MODBUS_MAX_SIZE;
			}
			buffer.write(readRawByteValues(address + currentPos, currentLength, modbus));
			currentPos += currentLength;
		}

		return buffer.toByteArray();
    }

    public static Calendar getCalendarFromDate(Date date, Modbus modBus) {
    		Calendar cal = ProtocolUtils.getCleanCalendar(modBus.gettimeZone());
    		cal.setTime(date);
    		return cal;
    }

	public static List<IntervalData> sortIntervalDatas(List<IntervalData> intervalDatas) {
		ProfileData pd = new ProfileData();
		pd.setIntervalDatas(intervalDatas);
		pd.sort();
		return pd.getIntervalDatas();
	}

	public static long intToLongUnsigned(int value) {
		return ((long)value) & 0x00000000FFFFFFFF;
	}

	// Hide utility class constructor
	private Utils() {}

}