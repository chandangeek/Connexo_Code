/*
 * ParseUtils.java
 *
 * Created on 30 november 2004, 15:24
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class ParseUtils {

    private static final int BIG_ENDIAN=0;
    private static final int LITTLE_ENDIAN=1;

    /** Creates a new instance of ParseUtils */
    public ParseUtils() {
    }

    /**
     * Convert a 32 or 24 bit BCD floating point number to a BigDecimal
     * E.M MM MM MM where M MM MM MM is the 7 digits mantissa and E is the exponent
     * @return BigDecimal value
     * @param data byte array
     * @param offset 0-based offset in byte array
     * @throws IOException thrown when an exception happens
     *
     * e.g. $01$61$11$68 floating BCD  --> 0.1611168 * 10^0 = 0.1611168

     */
    public static BigDecimal convertBCDFloatingPoint(byte data[], int offset, int length) throws IOException {
        byte[] subData = ProtocolUtils.getSubArray2(data,offset,length);
        int exponent = (((int)subData[0]&0xff)>>4)&0xf;
        subData[0] &=0xf;
        BigDecimal bd = convertBCDFixedPoint(subData,0,length,(length-1)*8+4);
        bd = bd.movePointRight(exponent);
        return bd;
    }

    /**
     * Convert a signed BCD fixed point number to a BigDecimal
     * @return BigDecimal value
     * @param data byte array
     * @param offset 0-based offset in byte array
     * @param length number of bytes of the fixed point number (max 8)
     * @param fpBitsRight nr of bits to the right of the decimal point
     * @throws IOException thrown when an exception happens
     *
     * e.g. 32 bit fixed point 1234.5678, 16 bits right oif decimal point
     *      length = 4
     *      fpBitsRight = 16
     */
    public static BigDecimal convertBCDFixedPoint(byte data[], int offset, int length, int fpBitsRight) throws IOException {
        return convertBCDFixedPoint(data,offset,length,BIG_ENDIAN,fpBitsRight);
    }
    public static BigDecimal convertBCDFixedPointLE(byte data[], int offset, int length, int fpBitsRight) throws IOException {
        return convertBCDFixedPoint(data,offset,length,LITTLE_ENDIAN,fpBitsRight);
    }
    public static BigDecimal convertBCDFixedPoint(byte data[], int offset, int length, int order, int fpBitsRight) throws IOException {
        byte[] subData = ProtocolUtils.getSubArray2(data,offset,length);
        int sign = (((int)subData[0] & 0xff) & 0x80)==0x80?-1:1;
        subData[0] &= 0x7f;
        long val;
        if (order == LITTLE_ENDIAN) {
            val = ParseUtils.getBCD2LongLE(subData, 0, length);
        } else {
            val = ParseUtils.getBCD2Long(subData, 0, length);
        }
        val = val*(sign);
        BigDecimal bd = BigDecimal.valueOf(val);
        bd = bd.movePointLeft(fpBitsRight/4);
        return bd;
    }



    /**
     * Convert a signed fixed point binary number to a BigDecimal
     * @return BigDecimal value
     * @param data byte array
     * @param offset 0-based offset in byte array
     * @param length number of bytes of the fixed point number (max 8)
     * @param fpBitsRight nr of bits to the right of the decimal point
     * @throws IOException thrown when an exception happens
     *
     * e.g. 32 bit fixed point AA BB . CC DD, 16 bits right oif decimal point
     *      length = 4
     *      fpBitsRight = 16
     */
    public static BigDecimal convertNormSignedFP2Number(byte data[], int offset, int length, int fpBitsRight) throws IOException {
        return convertNormSignedFP2Number(data,offset,length,BIG_ENDIAN,fpBitsRight);
    }
    public static BigDecimal convertNormSignedFP2NumberLE(byte data[], int offset, int length, int fpBitsRight) throws IOException {
        return convertNormSignedFP2Number(data,offset,length,LITTLE_ENDIAN,fpBitsRight);
    }

    public static BigDecimal convertNormSignedFP2Number(byte data[], int offset, int length, int order, int fpBitsRight) throws IOException {
        long temp=0;
        long lS=0;
        double val=0,lF=1;
        long sign=0x1;
        long mask=0;
        long decimal=0x1;

        if (length > 8) {
			throw new IOException("ParseUtils, convertNormSignedFP2Number, invalid length "+length);
		}

        if (order == LITTLE_ENDIAN) {
            temp = ProtocolUtils.getLongLE(data,offset,length);
        } else {
            temp = ProtocolUtils.getLong(data,offset,length);
        }

        // build sign
        sign <<= (length*8-1);

        // build decimal start fraction
        decimal <<= (fpBitsRight-1);

        // build mask
        for (int i=0;i<length;i++) {
			mask = (mask << 8) | 0xff;
		}

        // sign bit ?
        lS = ((temp & sign) != 0) ? -1 : 1;

        // take two's complement
        if (lS==-1) {
			temp = (temp^mask) + 1;
		}

        // calc normalized value
        for (int i=0;i<fpBitsRight;i++) {
           lF = lF /2;
           if ((temp & (decimal >> i)) != 0) {
			val+=lF;
		}
        }
        // if ((val==0) && (lS==-1)) val=1;
        // use sign bit

        temp >>= fpBitsRight;
        BigDecimal bd = BigDecimal.valueOf(temp).add(new BigDecimal(val)).multiply(BigDecimal.valueOf(lS));
        return bd;

    } // public static BigDecimal convertNormSignedFP2Number(byte data[], int offset, int length, int order, int fpBitsRight)

// This is code i got from Marc Hastings at Itron. It does exactly the same as the code in convertNormSignedFP2Number(...)
//    public static BigDecimal convertNormSignedFP2NumberNeg(byte rdata[], int offset, int length, int fpBitsRight) throws IOException {
//        byte[] uchBuffer = ProtocolUtils.getSubArray2(rdata,offset,length);
//        boolean sgn = false;
//        if ((((int)uchBuffer[0]&0x80) & 0x80)==0x80) {
//            sgn = true;
//            for( int i=0; i<6; i++ )
//            uchBuffer[i] = (byte)(~i(uchBuffer[i]));
//            uchBuffer[5] += 1;
//        }
//        double value = 0.0F;
//        for(int i=0; i<=5; i++) {
//            value *= 16.0F;
//            value  += (double) (i(uchBuffer[i]) >> 4);
//            value *= 16.0F;
//            value  += (double) (i(uchBuffer[i])  & 0x0F );
//        }
//        double denominator = 1.0F;
//        for(int i = (int) (fpBitsRight/4); i > 0; i-- )
//            denominator *= 16.0F;
//        value /=  denominator;
//
//        BigDecimal bd = new BigDecimal(value * ( sgn ? -1.0F : 1.0F ));
//        return bd;
//
//    } // public static BigDecimal convertNormSignedFP2NumberNeg(byte data[], int offset, int length, int order, int fpBitsRight)

    private static int i(byte val) {
        return (int)val&0xFF;
    }


    public static BigInteger getBigInteger(byte[] byteBuffer,int offset, int length) throws IOException {
        int shift = 0;
        BigInteger bi = BigInteger.ZERO;
        try {
            if (length > 64) {
				throw new IOException("ProtocolUtils, getBigIntegerLE, invalid length");
			}
            for (int i=0;i<length;i++) {
                bi = bi.multiply(BigInteger.valueOf(256));
                bi = bi.add(BigInteger.valueOf(((long)byteBuffer[offset+i]&0xffL)));
            }
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new IOException("ProtocolUtils, getBigIntegerLE, ArrayIndexOutOfBoundsException, "+e.getMessage());
        }
        return bi;
    }

    public static BigInteger getBigIntegerLE(byte[] byteBuffer,int offset, int length) throws IOException {
        int shift = 0;
        BigInteger bi = BigInteger.ZERO;
        try {
            if (length > 64) {
				throw new IOException("ProtocolUtils, getBigIntegerLE, invalid length");
			}
            for (int i=0;i<length;i++) {
                bi = bi.multiply(BigInteger.valueOf(256));
                bi = bi.add(BigInteger.valueOf(((long)byteBuffer[((length-1)-i)+offset]&0xffL)));
            }
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new IOException("ProtocolUtils, getBigIntegerLE, ArrayIndexOutOfBoundsException, "+e.getMessage());
        }
        return bi;
    }

    public static byte[] getArray(long val,int nrOfBytes) {
        byte[] data = new byte[nrOfBytes];
        for(int i=0;i<data.length;i++) {
            data[i] = (byte)((val >> (data.length-(i+1))*8)&0xFF);
        }
        return data;
    }
    public static byte[] getArrayLE(long val,int nrOfBytes) {
        byte[] data = new byte[nrOfBytes];
        for(int i=0;i<data.length;i++) {
            data[i] = (byte)(val >> (i*8));
        }
        return data;
    }


    // Adjust the year of timeUnderTest in order to match closest to the systemTime
    public static void adjustYear(Calendar systemTime, Calendar timeUnderTest) {
        int systemYear = systemTime.get(Calendar.YEAR);
        long diff=0;
        int saveOffset=-1;
        for (int offset = -1;offset <=1;offset++) {
            timeUnderTest.set(Calendar.YEAR,systemYear+offset);
            long timeUnderTestInMs = timeUnderTest.getTime().getTime();
            long systemTimeInMs = systemTime.getTime().getTime();
            long diff2 = Math.abs(timeUnderTestInMs-systemTimeInMs);
            if ((offset>-1) && (diff2 < diff)) {
                 saveOffset=offset;
            }
            diff = diff2;
        }
        timeUnderTest.set(Calendar.YEAR,systemYear+saveOffset);
    }

    // Adjust the year of timeUnderTest in order to match closest to the systemTime BUT timeUnderTest must <= systemTime !!
    public static void adjustYear2(Calendar systemTime, Calendar timeUnderTest) {
        int systemYear = systemTime.get(Calendar.YEAR);
        timeUnderTest.set(Calendar.YEAR,systemYear);
        if (timeUnderTest.getTime().after(systemTime.getTime())) {
            timeUnderTest.set(Calendar.YEAR,systemYear-1);
        }
    }

    /**
     * Extract an long value from the BCD byte array starting at offset for length.
     * @param byteBuffer byte array
     * @param offset offset
     * @param length length
     * @throws IOException Thrown when an exception happens
     * @return long value
     */
    public static long getBCD2Long(byte[] byteBuffer,int offset, int length) throws IOException {
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
     * Extract a int value from the BCD byte array starting at offset for length. The byte array is in little endial.
     * @return int value
     * @param byteBuffer byte array
     * @param offset 0-based offset in byte array
     * @param length number of bytes to use for calculation (min 1, max 4)
     * @throws IOException thrown when an exception happens
     */
    public static long getBCD2LongLE(byte[] byteBuffer,int offset, int length) throws IOException {
        long val=0;
        long multiplier=1;
        try {
            for(int i = offset; i < (offset+length) ; i++ ) {
               val += ((((ProtocolUtils.byte2int(byteBuffer[i]) >> 4) * 10) + (ProtocolUtils.byte2int(byteBuffer[i]) & 0x0F)) * multiplier);
               multiplier *= 100;
            }
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new IOException("ProtocolUtils, getBCD2IntLE, ArrayIndexOutOfBoundsException, "+e.getMessage());
        }
        return val;
    }

    public static void roundUp2nearestInterval(Calendar cal, int profileInterval) {
        int rest = (int)(cal.getTime().getTime()/1000) % profileInterval;
        if (rest > 0) {
			cal.add(Calendar.SECOND,profileInterval - rest);
		}
    }

    public static void roundDown2nearestInterval(Calendar cal, int profileInterval) {
        int rest = (int)(cal.getTime().getTime()/1000) % profileInterval;
        if (rest > 0) {
			cal.add(Calendar.SECOND,(-1)*rest);
		}
    }

    public static boolean isOnIntervalBoundary(Calendar cal, int profileInterval) {
        return (cal.getTime().getTime() % (profileInterval * 1000)) == 0;
    }

    public static void addIntervalValues(IntervalData intervalData,IntervalData intervalData2Add) {
        IntervalData tempIntervalData = new IntervalData(intervalData.getEndTime());
        for (int i = 0 ; i < intervalData.getIntervalValues().size() ; i++) {
            int current = intervalData2Add.get(i).intValue() + intervalData.get(i).intValue();
            tempIntervalData.addValue(new Integer(current));
        }
        intervalData.setIntervalValues(tempIntervalData.getIntervalValues());
    }


    public static int getNrOfDays(Date from, Date to, TimeZone timeZone) throws IOException {
        if (to.getTime() < from.getTime()) {
			throw new IOException("ParseUtils, getNrOfDays, error ("+from+") > ("+to+")");
		}
        long offset = to.getTime() - from.getTime();
        final long ONEDAY=24*60*60*1000;
        long tostd = to.getTime() + (long)timeZone.getOffset(to.getTime());
        long fromstd = from.getTime() + (long)timeZone.getOffset(from.getTime());
        long nrOfDaysToRetrieve = ((tostd/ONEDAY) - (fromstd/ONEDAY)) + 1;
        return (int)nrOfDaysToRetrieve;
    }



    /**
     * returns a sub array from index to end
     * @param data source array
     * @param from from index
     * @return subarray
     */
    public static byte[] getSubArray(byte[] data,int from) {
        byte[] subArray = new byte[data.length-from];
        for (int i=0;i<subArray.length;i++) {
           subArray[i] = data[i+from];
        }
        return subArray;
    }

    /**
     * returns a sub array from index to end
     * @param data source array
     * @param from from index
     * @return subarray
     */
    public static byte[] getSubArray(int[] data,int from) {
        byte[] subArray = new byte[data.length-from];
        for (int i=0;i<subArray.length;i++) {
           subArray[i] = (byte)data[i+from];
        }
        return subArray;
    }

    /**
     * returns a sub array from index to end
     * @param data source array
     * @param from from index
     * @param length length to copy
     * @return subarray
     */
    public static int[] getSubArray(int[] data,int from,int length) {
        int[] subArray = new int[length];
        for (int i=0;i<subArray.length;i++) {
           subArray[i] = data[i+from];
        }
        return subArray;
    }

    /**
     * returns a sub array from offset to offset + length
     * @param data source array
     * @param offset from index
     * @param length length of sub array
     * @return subarray
     */
    public static byte[] getSubArray(byte[] data,int offset, int length) {
        byte[] subArray = new byte[length];
        for (int i=0;i<subArray.length;i++) {
           subArray[i] = data[i+offset];
        }
        return subArray;
    }
    public static byte[] getSubArrayLE(byte[] data,int offset, int length) {
        byte[] subArray = new byte[length];
        for (int i=0;i<subArray.length;i++) {
           subArray[i] = data[((subArray.length-1)-i)+offset];
        }
        return subArray;
    }


    public static byte[] convert2ByteArray(int[] data,int from) {
        byte[] subArray = new byte[(data.length-from)*2];
        for (int i=0;i<subArray.length;i+=2) {
           subArray[i] = (byte)(data[i+from]/256);
           subArray[i+1] = (byte)(data[i+from]%256);
        }
        return subArray;
    }


    /**
     *   Build a hexadecimal String representation from a long value and right space-extend the value to length.
     *   E.g. buildStringHex(10,4) returns "A   " String
     * @param value Value to convert
     * @param length length of the String
     * @return 0-extended String value
     */
    public static String buildStringHexExtendedWithSpaces(long value,int length) {
        String str=Long.toHexString(value);
        StringBuffer strbuff = new StringBuffer();
        strbuff.append(str);
        if (length >= str.length()) {
			for (int i=0;i<(length-str.length());i++) {
				strbuff.append(' ');
			}
		}
        return strbuff.toString().toUpperCase();
    }
    /**
     *   Build a decimal String representation from a long value and right space-extend the value to length.
     *   E.g. buildStringHex(10,4) returns "A   " String
     * @param value Value to convert
     * @param length length of the String
     * @return 0-extended String value
     */
    public static String buildStringDecimalExtendedWithSpaces(long value,int length) {
        String str=Long.toString(value);
        StringBuffer strbuff = new StringBuffer();
        strbuff.append(str);
        if (length >= str.length()) {
			for (int i=0;i<(length-str.length());i++) {
				strbuff.append(' ');
			}
		}
        return strbuff.toString();
    }

    public static String buildBinaryRepresentation(long val, int nrOfBits) {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<nrOfBits;i++) {
            if ((val & (0x1<<((nrOfBits-1)-i))) != 0) {
				strBuff.append("1");
			} else {
				strBuff.append("0");
			}
        }
        return strBuff.toString();
    }

    public static byte[] extendWithNULL(byte[] rdata,int length) {
        byte[] data = new byte[length];
        System.arraycopy(rdata, 0, data, 0, rdata.length);
        for (int i=rdata.length;i<data.length;i++) {
			data[i]=0;
		}
        return data;

    }
    public static byte[] extendWithChar0(byte[] rdata,int length) {
        byte[] data = new byte[length];
        System.arraycopy(rdata, 0, data, 0, rdata.length);
        for (int i=rdata.length;i<data.length;i++) {
			data[i]=0x30;
		}
        return data;

    }
    public static byte[] extendWithWhiteSpace(byte[] rdata,int length) {
        byte[] data = new byte[length];
        System.arraycopy(rdata, 0, data, 0, rdata.length);
        for (int i=rdata.length;i<data.length;i++) {
			data[i]=0x20;
		}
        return data;

    }
    public static byte[] extendWithBinary0(byte[] rdata,int length) {
        byte[] data = new byte[length*2];
        System.arraycopy(rdata, 0, data, 0, rdata.length);
        for (int i=rdata.length;i<data.length;i+=2) {
            data[i]=0x33;
            data[i+1]=0x30;
        }
        return data;

    }



    public static long signExtend(long value, int nrOfBitsOfBase) {
        if ((value & (1<<(nrOfBitsOfBase-1))) != 0) {
            for (int i=nrOfBitsOfBase ; i<64;i++) {
                value |= (1L<<i);
            }
        }
        return value;
    }

    public static byte[] createBCDByteArray(long val,int nrOfBCDDigits) {
    	int arrayLength = nrOfBCDDigits/2+nrOfBCDDigits%2;
    	byte[] data = new byte[arrayLength];
    	for (int i=0;i<arrayLength;i++) {
        	long modulo = Double.valueOf(Math.pow(100,arrayLength-i)).longValue();
        	long divide = Double.valueOf(Math.pow(100,(arrayLength-1)-i)).longValue();
        	long val2 = (val % modulo) / divide;
    		data[i] = ProtocolUtils.hex2BCD((byte)val2);
    	}
    	return data;
    }
    public static byte[] createBCDByteArrayLE(long val,int nrOfBCDDigits) {
    	int arrayLength = nrOfBCDDigits/2+nrOfBCDDigits%2;
    	byte[] data = new byte[arrayLength];
    	for (int i=0;i<arrayLength;i++) {
        	long modulo = Double.valueOf(Math.pow(100,i+1)).longValue();
        	long divide = Double.valueOf(Math.pow(100,i)).longValue();
        	long val2 = (val % modulo) / divide;
    		data[i] = ProtocolUtils.hex2BCD((byte)val2);
    	}
    	return data;
    }


    public static byte[] applyMask(byte[] temp, long mask) {
        for (int i=0;i<temp.length;i++) {
       	 	temp[i] |= (mask >> (8*i));
        }
        return temp;
    }

    public static byte[] createBCDByteArrayLEWithMask(String val,int nrOfBCDDigits) {
    	return createBCDByteArrayLEWithMask(Long.parseLong(val,16),nrOfBCDDigits);
    }
    public static byte[] createBCDByteArrayLEWithMask(long val,int nrOfBCDDigits) {
    	int arrayLength = nrOfBCDDigits/2+nrOfBCDDigits%2;
    	byte[] data = new byte[arrayLength];
    	for (int i=0;i<arrayLength;i++) {
    		data[i] = (byte)(val >> (8*i));
    	}
    	return data;
    }

    public static byte[] stripByteArrayBrackets(byte[] byteArrayWithBrackets){
    	return (ProtocolUtils.stripBrackets(new String(byteArrayWithBrackets))).getBytes();
    }

    public static byte[] hexStringToByteArray(String str) {
        if (str.length() == 1) {
            str = "0" + str;
        }
        byte[] data = new byte[str.length() / 2];
        int offset = 0;
        int endOffset = 2;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) Integer.parseInt(str.substring(offset, endOffset), 16);
            offset = endOffset;
            endOffset += 2;
        }
        return data;
    }

    /**
     * Build up a stringbuffer containing the hex values from the byteArray.
     * Adds zero to the left if necessary.
     * ex:
     * b = {7, 1, 67, 7};
     * strByff.toString() = "07014307";
     * @param byteArray - the byteArray containing the ascii chars
     * @return
     */
    public static String decimalByteToHexString(byte[] byteArray){
        StringBuilder builder = new StringBuilder();
        for (byte aB : byteArray) {
            String str = Integer.toHexString(aB & 0xFF);
            if (str.length() == 1) {
                builder.append("0");
            }
            builder.append(str);
        }
        return builder.toString();
    }

}