/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.generic;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.Register;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/**
 *
 * @author kvds
 */
public class ParseUtils {

    /** Creates a new instance of ParseUtils */
    public ParseUtils() {
    }


    /**
     *   Build a decimal String representation from an int value an 0-extend the value to length.
     *   E.g. buildStringHex(10,4) returns "0010" String
     * @param value Value to convert
     * @param length length of the String
     * @return 0-extended String value
     */
    public static String buildStringDecimal(int value,int length) {
        String str=Integer.toString(value);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length()) {
			for (int i=0;i<(length-str.length());i++) {
				strbuff.append('0');
			}
		}
        strbuff.append(str);
        return strbuff.toString();
    }
    /**
     *   Build a decimal String representation from an int value an 0-extend the value to length.
     *   E.g. buildStringHex(10,4) returns "0010" String
     * @param value Value to convert
     * @param length length of the String
     * @return 0-extended String value
     */
    public static String buildStringDecimal(long value,int length) {
        String str=Long.toString(value);
        StringBuffer strbuff = new StringBuffer();
        if (length >= str.length()) {
			for (int i=0;i<(length-str.length());i++) {
				strbuff.append('0');
			}
		}
        strbuff.append(str);
        return strbuff.toString();
    }



    /**
     * Extract an long value from the BCD byte array starting at offset for length.
     * @param byteBuffer byte array
     * @param offset offset
     * @param length length
     * @throws java.io.IOException Thrown when an exception happens
     * @return long value
     */
    public static long bcd2Long(byte[] byteBuffer,int offset, int length) throws IOException {
        long val=0;
        long multiplier=1;
        try {
            for(int i = ((offset+length)-1); i >= offset ; i-- ) {
                val += ((((ProtocolUtils.byte2int(byteBuffer[i]) >> 4) * 10) + (ProtocolUtils.byte2int(byteBuffer[i]) & 0x0F)) * multiplier);
                multiplier *= 100;
            }
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new IOException("ProtocolUtils, getBCD2IntLE, ArrayIndexOutOfBoundsException, "+e.getMessage());
        }
        return val;
    }

    /**
     * Checks if a string can be parsed to an integer
     * @param str - the String to check
     * @return true or false
     */
    public static boolean isInteger(String str){
    	try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
    }

    /**
     * Checks if a string can be parsed to an integer
     * @param str - the String to check
     * @return true or false
     */
    public static boolean isLong(String str){
    	try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
    }

    /**
     * Build up a stringbuffer containing the hex values from the byteArray.
     * Adds zero to the left if necessary.
     * ex:
     * b = {7, 1, 67, 7};
     * strByff.toString() = "07014307";
     * @param b - the byteArray containing the ascii chars
     * @return
     */
    public static String decimalByteToString(byte[] b){
		StringBuffer strBuff = new StringBuffer();
		for(int i = 0; i < b.length; i++){
			String str = Integer.toHexString(b[i]&0xFF);
			if(str.length() == 1) {
				strBuff.append("0");
			}
			strBuff.append(str);
		}
		return strBuff.toString();
    }

    public static byte[] hexStringToByteArray(String str){
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


    /**
     * Checks if all the elements in the byteArray can be converted to valid chars.
     * Only decimals and the complete upper- and lower alphabet is allowed
     * @param b - the given byteArray
     * @return true or false
     */
    public static boolean checkIfAllAreChars(byte[] b){
    	for(int i = 0; i < b.length; i++){
    		if(!(b[i] >= 48 && b[i] <= 57) && !(b[i] >= 65 && b[i] <= 90) && !(b[i] >= 97 && b[i] <= 122)) {
				return false;
			}
    	}
    	return true;
    }

    public static boolean checkIfAllAreDecimalChars(byte[] b){
    	for(int i = 0; i < b.length; i++){
    		if(!(b[i] >= 48 && b[i] <= 57)) {
				return false;
			}
    	}
    	return true;
    }

    /**
     * Create a midnight date from one month ago
     * @param rtu
     * @return
     */
	public static Date getClearLastMonthDate(BaseDevice rtu) {
        return getClearLastMonthDate(TimeZone.getDefault());
	}

    /**
     * Create a midnight date from one month ago
     * @param deviceTimeZone
     * @return
     */
    public static Date getClearLastMonthDate(TimeZone deviceTimeZone) {
   		Calendar tempCalendar = Calendar.getInstance(deviceTimeZone != null ? deviceTimeZone : TimeZone.getDefault());
   		tempCalendar.add(Calendar.MONTH, -1);
		tempCalendar.set(Calendar.HOUR_OF_DAY, 0 );
		tempCalendar.set(Calendar.MINUTE, 0 );
		tempCalendar.set(Calendar.SECOND, 0 );
		tempCalendar.set(Calendar.MILLISECOND, 0 );
		return tempCalendar.getTime();
	}

    /**
     * Create a midnight date from one month ago
     * @param deviceTimeZone
     * @return
     */
    public static Date getClearLastDayDate(TimeZone deviceTimeZone) {
   		Calendar tempCalendar = Calendar.getInstance(deviceTimeZone != null ? deviceTimeZone : TimeZone.getDefault());
   		tempCalendar.add(Calendar.DAY_OF_YEAR, -1);
		tempCalendar.set(Calendar.HOUR_OF_DAY, 0 );
		tempCalendar.set(Calendar.MINUTE, 0 );
		tempCalendar.set(Calendar.SECOND, 0 );
		tempCalendar.set(Calendar.MILLISECOND, 0 );
		return tempCalendar.getTime();
	}

	/**
	 * ex: "1.0.0.8.1.255" has 5 dots '.'
	 * @param str - the complete string
	 * @param sign - the string to count
	 * @return
	 */
	public static int countEqualSignsInString(String str, String sign){
		int count  = 0;
		byte[] strByte = str.getBytes();
		byte[] signByte = sign.getBytes();
		for(int i = 0; i < strByte.length; i++){
			if(strByte[i] == signByte[0]){
				count++;
			}
		}
		return count;
	}

	/**
	 * Convert a DLMS register to a quantity
	 * @param register
	 * @return the quantity from the register
	 * @throws java.io.IOException
	 */
	public static Quantity registerToQuantity(Register register) throws IOException{
		try {
			if(register.getScalerUnit() != null){
				if(register.getScalerUnit().getUnitCode() != 0){

                    //The EIServer Percent Unit code (515) does not match the DLMS Unit code (56)
                    if(register.getScalerUnit().getUnitCode() == 56){
                        return new Quantity(BigDecimal.valueOf(register.getValue()), Unit.get(BaseUnit.PERCENT));
                    } else {
                        return new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit());
                    }
				} else {
					return new Quantity(BigDecimal.valueOf(register.getValue()), Unit.get(BaseUnit.UNITLESS));
				}
			} else {
				return new Quantity(BigDecimal.valueOf(register.getValue()), Unit.get(BaseUnit.UNITLESS));
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Convert a DLMS object to a quantity
     *
     * @param cosemObject
     * @return
     * @throws java.io.IOException
     */
    public static Quantity cosemObjectToQuantity(CosemObject cosemObject) throws IOException{
		try {
			if(cosemObject.getScalerUnit() != null){
				if(cosemObject.getScalerUnit().getUnitCode() != 0){
					return new Quantity(BigDecimal.valueOf(cosemObject.getValue()), cosemObject.getScalerUnit().getEisUnit());
				} else {
					return new Quantity(BigDecimal.valueOf(cosemObject.getValue()), Unit.get(BaseUnit.UNITLESS));
				}
			} else {
				return new Quantity(BigDecimal.valueOf(cosemObject.getValue()), Unit.get(BaseUnit.UNITLESS));
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

    public static void validateProfileData(ProfileData profileData, Date date) {
        Iterator it = profileData.getIntervalDatas().iterator();
        while (it.hasNext()) {
            IntervalData ivdt = (IntervalData) it.next();
            if (ivdt.getEndTime().after(date)) {
                //System.out.println("KV_DEBUG> remove "+ivdt);
                it.remove();
            }
        }
    }

	public AXDRDateTime convertUnixToGMTDateTime(String time) throws IOException{
		try {
			AXDRDateTime dateTime = null;
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.setTimeInMillis(Long.parseLong(time)*1000);
			dateTime = new AXDRDateTime(cal);
			return dateTime;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("Could not parse " + time + " to a long value");
		}
	}

}
