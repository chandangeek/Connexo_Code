package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

class Utils {

	
	
	static int toInt(final byte val) {
		return ((int)val & 0xff);
	}
	
	static int toInt(final short val) {
		return ((int)val & 0xffff);
	}
	
	static String toHexString(final byte val) {
		return toHexString(toInt(val));
	}
	
	static String toHexString(final int val) {
		return "0x"+Integer.toHexString(val);
	}

	static String toHexString(final long val) {
		return "0x"+Long.toHexString(val);
	}
	
	
    /**
     * returns a sub array from index to end
     * @param data source array
     * @param from from index
     * @return subarray
     */
    static byte[] getSubArray(final byte[] data, final int offset) {
        byte[] subArray = new byte[data.length-offset];
        System.arraycopy(data,offset,subArray,0,subArray.length);
        return subArray;
    }
    
    /**
     * returns a sub array from index to end
     * @param data source array
     * @param from from index
     * @param length 
     * @return subarray
     */
    static byte[] getSubArray(final byte[] data,final int offset, final int length) {
        byte[] subArray = new byte[length];
        System.arraycopy(data,offset,subArray,0,subArray.length);
        return subArray;
    }	
}

