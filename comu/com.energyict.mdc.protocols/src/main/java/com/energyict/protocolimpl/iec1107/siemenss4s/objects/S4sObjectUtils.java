package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 * Useful utils for operations with the S4s data
 * @author gna
 *
 */
public class S4sObjectUtils {

	/**
	 * Switches the nibbles in the byteArray
	 * @param lsbNibbleByte
	 * @return
	 */
	public static byte[] switchNibbles(byte[] lsbNibbleByte){
		int offset = 0;
		StringBuffer strBuff = new StringBuffer();
		StringBuffer strBuff2 = new StringBuffer();
		for(int i = 0; i < lsbNibbleByte.length/2; i++){
			strBuff = new StringBuffer();
			strBuff.append(new String(ProtocolUtils.getSubArray2(lsbNibbleByte, i+offset+1, 1)));
			strBuff.append(new String(ProtocolUtils.getSubArray2(lsbNibbleByte, i+offset, 1)));
			strBuff2.append(strBuff.toString());
			offset += 1;
		}
		return strBuff2.toString().getBytes();
	}

	/**
	 * Revert from LSB-MBS to MSB-LSB
	 * @param array - the Array to revert
	 * @return an inverted array
	 */
	public static byte[] revertByteArray(byte[] array){
		byte[] reverse = new byte[array.length];
		int offset = array.length-1;
		for(int i = 0; i < array.length; i++,offset--) {
			reverse[i] = array[offset];
		}

		return reverse;
	}


	/**
	 * Convert a byteArray with ASCII values of a decimal number to a byteArray forming decimal numbers
	 * @param data ex. [56, 48, 57, 48, 52, 50]
	 * @return the converted byteArray ex. [80, 90, 42]
	 */
	public static byte[] getAsciiConvertedDecimalByteArray(byte[] data){
		if(data.length%2 != 0){
			throw new IllegalArgumentException("Data length is not even.");
		}
		byte[] converted = new byte[data.length/2];
		String temp = new String(data);
		String tempPart;
		for(int i = 0; i < converted.length; i++){
			tempPart = temp.substring(i*2, (i*2)+2);
			converted[i] = Integer.valueOf(tempPart).byteValue();
		}
		return converted;
	}

	/**
	 * Check whether the recordData contains a date or if its just an interval.
	 * The fist BIT of the 4th last BYTE is 1 if it contains a date.
	 * @param recordData - raw bytes containing the intervalData
	 * @return true if its a date interval, otherwise false
	 * @throws IOException - if it's invalid Hex data
	 */
	public static boolean itsActuallyADateIntervalRecord(byte[] recordData) throws IOException{
		if((ProtocolUtils.hex2nibble(recordData[recordData.length-4])&0x01) == 1){
			return true;
		} else {
			return false;
		}
	}

	public static byte[] hexStringToByteArray(String str){
		if(str.length() == 1){
			str = "0"+str;
		}
		byte[] data = new byte[str.length()/2];
		int offset = 0;
		int endOffset = 2;
		for(int i = 0; i < data.length; i++){
			data[i] = (byte)Integer.parseInt(str.substring(offset, endOffset), 16);
			offset = endOffset;
			endOffset += 2;
		}
		return data;
	}
}
