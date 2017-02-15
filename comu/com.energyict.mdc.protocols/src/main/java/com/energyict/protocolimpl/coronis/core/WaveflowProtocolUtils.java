/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class WaveflowProtocolUtils {

	
	
	static public int toInt(final byte val) {
		return ((int)val & 0xff);
	}
	
	static public int toInt(final short val) { 
		return ((int)val & 0xffff);
	}
	
	static public String toHexString(final byte val) {
		return toHexString(toInt(val));
	}
	
	static public String toHexString(final short val) {
		return "0x"+ Integer.toHexString(toInt(val));
	}
	
	static public String toHexString(final int val) {
		return "0x"+ Integer.toHexString(val);
	}

	static public String toHexString(final long val) {
		return "0x"+ Long.toHexString(val);
	}
	
	
    /**
     * returns a sub array from index to end
     * @param data source array
     * @param from from index
     * @return subarray
     */
    static public byte[] getSubArray(final byte[] data, final int offset) {
        byte[] subArray = new byte[data.length-offset];
        System.arraycopy(data, offset, subArray, 0, subArray.length);
        return subArray;
    }
    
    /**
     * returns a sub array from index to end
     * @param data source array
     * @param from from index
     * @param length 
     * @return subarray
     */
    static public byte[] getSubArray(final byte[] data,final int offset, final int length) {
        byte[] subArray = new byte[length];
        System.arraycopy(data, offset, subArray, 0, subArray.length);
        return subArray;
    }

    static public int parseInt(String value) throws IOException {
		try {
			return Integer.parseInt(value, 10);
		}
		catch(NumberFormatException e) {
			try {
				if (value.toUpperCase().indexOf("0X") == 0) {
					value = value.substring(2);
				}
				return Integer.parseInt(value, 16);
			}
			catch(NumberFormatException ex) {
				throw new IOException("Number format error. Cannot parse ["+value+"] to int!");
			}
		}    
    }

    
    
    static public byte[] getArrayFromStringHexNotation(String str) throws IOException {
		if ((str.length()%2) != 0) {
			throw new IOException("Invalid string to parse to byte array ["+str+"]");
		}
		
		byte[] array = new byte[str.length()/2];
		for (int i=0;i<array.length;i++) {
			array[i] = (byte)(Integer.parseInt(str.substring(i * 2, (i * 2) + 2), 16) & 0xFF);
		}
		return array;
	}
}

