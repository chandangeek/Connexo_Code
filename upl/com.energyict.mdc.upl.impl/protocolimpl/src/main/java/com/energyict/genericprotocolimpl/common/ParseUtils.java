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

import com.energyict.protocol.*;
import java.io.*;

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
        
}
