/*
 * ParseUtils.java
 *
 * Created on 20 december 2007, 9:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.common;

import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.*;
import java.io.*;
import java.util.Calendar;
import java.util.Date;

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
        if (length >= str.length())
            for (int i=0;i<(length-str.length());i++)
                strbuff.append('0');
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
        if (length >= str.length())
            for (int i=0;i<(length-str.length());i++)
                strbuff.append('0');
        strbuff.append(str);
        return strbuff.toString();
    }
    
    
    
    /**
     * Extract an long value from the BCD byte array starting at offset for length.
     * @param byteBuffer byte array
     * @param offset offset
     * @param length length
     * @throws IOException Thrown when an exception happens
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
			if(str.length() == 1)
				strBuff.append("0");
			strBuff.append(str);
		}
		return strBuff.toString();
    }
    
    
    /**
     * Checks if all the elements in the byteArray can be converted to valid chars.
     * Only decimals and the complete upper- and lower alphabet is allowed
     * @param b - the given byteArray
     * @return true or false
     */
    public static boolean checkIfAllAreChars(byte[] b){
    	for(int i = 0; i < b.length; i++){
    		if(!(b[i] >= 48 && b[i] <= 57) && !(b[i] >= 65 && b[i] <= 90) && !(b[i] >= 97 && b[i] <= 122))
    			return false;
    	}
    	return true;
    }
    
    public static boolean checkIfAllAreDecimalChars(byte[] b){
    	for(int i = 0; i < b.length; i++){
    		if(!(b[i] >= 48 && b[i] <= 57))
    			return false;
    	}
    	return true;
    }
        
    /**
     * Create a midnight date from one month ago
     * @param rtu
     * @return
     */
	public static Date getClearLastMonthDate(Rtu rtu) {
   		Calendar tempCalendar = Calendar.getInstance(rtu.getDeviceTimeZone());
   		tempCalendar.add(Calendar.MONTH, -1);
		tempCalendar.set(Calendar.HOUR_OF_DAY, 0 );
		tempCalendar.set(Calendar.MINUTE, 0 );
		tempCalendar.set(Calendar.SECOND, 0 );
		tempCalendar.set(Calendar.MILLISECOND, 0 );
		return tempCalendar.getTime();
	}
	
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
	
	public static void main(String[] args){
		String str = "99.1.0";
		String sgn = ".";
		System.out.println(countEqualSignsInString(str, sgn));
	}
}
