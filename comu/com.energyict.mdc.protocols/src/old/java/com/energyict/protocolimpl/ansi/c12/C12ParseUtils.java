/*
 * C12ParseUtils.java
 *
 * Created on 25 oktober 2005, 5:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class C12ParseUtils {

    /** Creates a new instance of C12ParseUtils */
    public C12ParseUtils() {
    }

    public static int getDateSize() {
        return 2;
    }

    public static Date getDateFromDate(byte[] data, int offset, TimeZone timeZone, int dataOrder) throws IOException {
        int temp = getInt(data,offset,2,dataOrder);
        int year = temp & 0x007F;
        int month = (temp >> 7) & 0x000F;
        int day =  (temp >> 11) & 0x001F;
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        if (year < 100) {
            cal.set(Calendar.YEAR, year >= 90 ? year + 1990 : year + 2000);
        }
        if ((month > 0) && (month < 13)) {
            cal.set(Calendar.MONTH, month - 1);
        }
        if (day > 0) {
            cal.set(Calendar.DAY_OF_MONTH, day);
        }
        return cal.getTime();
    }

    public static int getLTimeSize(int timeFormat) throws IOException {
        switch(timeFormat) {
            case 0:
                throw new IOException("C12ParseUtils, getLTimeSize, timeformat != 0 expected! Cannot continue!");

            case 1:
            case 2:
                return 6;

            case 3:
                return 5;

            default:
                throw new IOException("C12ParseUtils, getLTimeSize, invalid timeformat, "+timeFormat+" ! Cannot continue!");
        }

    } // public static int getLTimeSize(int timeFormat) throws IOException

    public static Date getDateFromLTimeAndAdjustForTimeZone(byte[] data, int offset, int timeFormat, TimeZone timeZone, int dataOrder) throws IOException {
        if (timeFormat==3) {
            return adjustBlockEndTime(getDateFromLTime(data, offset, timeFormat, timeZone, dataOrder), timeZone);
        }
        else {
            return getDateFromLTime(data, offset, timeFormat, timeZone, dataOrder);
        }
    }

    public static Date getDateFromLTime(byte[] data, int offset, int timeFormat, TimeZone timeZone, int dataOrder) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        switch(timeFormat) {
            case 0:
                throw new IOException("C12ParseUtils, getDateFromLTime, timeformat != 0 expected! Cannot continue!");

            case 1: {
                int year = ProtocolUtils.BCD2hex(data[offset]);
                cal.set(Calendar.YEAR,year<90?year+2000:year+1900);
                cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset+1])-1);
                cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset+2]));
                cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset+3]));
                cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset+4]));
                cal.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[offset+5]));
            } break;

            case 2: {
                int year = (int)data[offset]&0xFF;
                cal.set(Calendar.YEAR,year<90?year+2000:year+1900);
                cal.set(Calendar.MONTH,(int)data[offset+1]-1);
                cal.set(Calendar.DAY_OF_MONTH,data[offset+2]);
                cal.set(Calendar.HOUR_OF_DAY,data[offset+3]);
                cal.set(Calendar.MINUTE,data[offset+4]);
                cal.set(Calendar.SECOND,data[offset+5]);
            } break;

            case 3: {
                long minutes = getLong(data,offset,4,dataOrder);
                int seconds = getInt(data,offset+4);
                Date date = new Date((minutes*60+seconds)*1000);
                cal.setTime(date);

            } break;

            default:
                throw new IOException("C12ParseUtils, getDateFromLTime, invalid timeformat, "+timeFormat+" ! Cannot continue!");
        }

        return cal.getTime();
    } // public static Date getDateFromLTime(byte[] data, int offset, int timeFormat, TimeZone timeZone) throws IOException

    public static int getSTimeSize(int timeFormat) throws IOException {
        switch(timeFormat) {
            case 0:
                throw new IOException("C12ParseUtils, getSTimeSize, timeformat != 0 expected! Cannot continue!");

            case 1:
            case 2:
                return 5;

            case 3:
                return 4;

            default:
                throw new IOException("C12ParseUtils, getSTimeSize, invalid timeformat, "+timeFormat+" ! Cannot continue!");
        }

    }

    public static Date getDateFromSTimeAndAdjustForTimeZone(byte[] data, int offset, int timeFormat, TimeZone timeZone, int dataOrder) throws IOException {
        if (timeFormat==3) {
            return adjustBlockEndTime(getDateFromSTime(data, offset, timeFormat, timeZone, dataOrder), timeZone);
        }
        else {
            return getDateFromSTime(data, offset, timeFormat, timeZone, dataOrder);
        }
    }

    // Due to a spec non-conformity in the Sentinel meter for the UDATE (GMT minutes from 1970)
    private static Date adjustBlockEndTime(Date date, TimeZone timeZone) {
        if (timeZone.inDaylightTime(date)) {
            return new Date(date.getTime() - (timeZone.getRawOffset() + 3600000));
        }
        else {
            return new Date(date.getTime() - timeZone.getRawOffset());
        }

    }


    public static Date getDateFromSTime(byte[] data, int offset, int timeFormat, TimeZone timeZone, int dataOrder) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        switch(timeFormat) {
            case 0:
                throw new IOException("C12ParseUtils, getDateFromSTime, timeformat != 0 expected! Cannot continue!");

            case 1: {
                int year = ProtocolUtils.BCD2hex(data[offset]);
                cal.set(Calendar.YEAR,year<90?year+2000:year+1900);
                cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset+1])-1);
                cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset+2]));
                cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset+3]));
                cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset+4]));
            } break;

            case 2: {
                int year = (int)data[offset]&0xFF;
                cal.set(Calendar.YEAR,year<90?year+2000:year+1900);
                cal.set(Calendar.MONTH,(int)data[offset+1]-1);
                cal.set(Calendar.DAY_OF_MONTH,data[offset+2]);
                cal.set(Calendar.HOUR_OF_DAY,data[offset+3]);
                cal.set(Calendar.MINUTE,data[offset+4]);
            } break;

            case 3: {
                long minutes = getLong(data,offset,4,dataOrder);
                Date date = new Date((minutes*60)*1000);
                cal.setTime(date); //calGMT.getTime());
            } break;

            default:
                throw new IOException("C12ParseUtils, getDateFromSTime, invalid timeformat, "+timeFormat+" ! Cannot continue!");
        }

        return cal.getTime();
    }

    public static int getTimeSize(int timeFormat) throws IOException {
        switch(timeFormat) {
            case 0:
                throw new IOException("C12ParseUtils, getTimeSize, timeformat != 0 expected! Cannot continue!");

            case 1:
            case 2:
                return 3;

            case 3:
                return 4;

            default:
                throw new IOException("C12ParseUtils, getTimeSize, invalid timeformat, "+timeFormat+" ! Cannot continue!");
        }

    }

    public static Date getDateFromTime(byte[] data, int offset, int timeFormat, TimeZone timeZone, int dataOrder) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        switch(timeFormat) {
            case 0:
                throw new IOException("C12ParseUtils, getDateFromTime, timeformat != 0 expected! Cannot continue!");

            case 1: {
                cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset]));
                cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset+1]));
                cal.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[offset+2]));
            } break;

            case 2: {
                cal.set(Calendar.HOUR_OF_DAY,data[offset]);
                cal.set(Calendar.MINUTE,data[offset+1]);
                cal.set(Calendar.SECOND,data[offset+2]);
            } break;

            case 3: {
                Calendar calGMT = ProtocolUtils.getCleanGMTCalendar();
                long minutes = getLong(data,offset,4,dataOrder);
                Date date = new Date(minutes*60*1000);
                calGMT.setTime(date);
                cal.setTime(calGMT.getTime());
            } break;

            default:
                throw new IOException("C12ParseUtils, getDateFromTime, invalid timeformat, "+timeFormat+" ! Cannot continue!");
        }

        return cal.getTime();
    }

    public static int getNonIntegerSize(int niFormat) throws IOException {
        switch(niFormat) {
            case 0: { // FLOAT64
                return 8;
            }
            case 1: { // FLOAT32
                return 4;
            }
            case 2: { // char[12]
                return 12;
            }

            case 3: { // char[6]
                return 6;
            }

            case 4: { // int32 with decimal point between fourth and fifth digit!
                return 4;
            }

            case 5: { // array 6 of bcd, becomes 12 bytes
                return 6;
            }

            case 6: { // array 4 of bcd, becomes 8 bytes
                return 4;
            }

            case 7: { // int24
                return 3;
            }

            case 8: { // int32
                return 4;
            }

            case 9: { // int40
                return 5;
            }

            case 10: { // int48
                return 6;
            }

            case 11: { // int64
                return 8;
            }

            default:
                throw new IOException("C12ParseUtils, getNonIntegerSize, invalid non integer format, "+niFormat+" ! Cannot continue!");
        }
    }

    public static final int FORMAT_INT64=11;

    public static Number getNumberFromNonInteger(byte[] data, int offset, int niFormat, int dataOrder) throws IOException {
        switch(niFormat) {
            case 0: { // FLOAT64
                long val = C12ParseUtils.getLong(data,offset, 8, dataOrder);
                return new BigDecimal(Double.longBitsToDouble(val));
            }
            case 1: { // FLOAT32
                int val = C12ParseUtils.getInt(data,offset, 4, dataOrder);
                return new BigDecimal((double)Float.intBitsToFloat(val));
            }
            case 2: { // char[12]
                String str = new String(ProtocolUtils.getSubArray2(data, offset, 12));
                return new BigDecimal(str);
            }

            case 3: { // char[6]
                String str = new String(ProtocolUtils.getSubArray2(data, offset, 6));
                return new BigDecimal(str);
            }

            case 4: { // int32 with decimal point between fourth and fifth digit!
                return new BigDecimal(new BigInteger(Long.toString(getExtendedLong(data,offset,4,dataOrder))),4);
            }

            case 5: { // array 6 of bcd, becomes 12 bytes

                return getBCD2BigDecimal(data, offset, 6, 1); // dataorder always MSB first!
                //byte[] tmp = ProtocolUtils.convertAscii2Binary(ProtocolUtils.getSubArray2(data, offset, 6));
                //return new BigDecimal(new String(tmp));
            }

            case 6: { // array 4 of bcd, becomes 8 bytes
                return getBCD2BigDecimal(data, offset, 4, 1); // dataorder always MSB first!
                //byte[] tmp = ProtocolUtils.convertAscii2Binary(ProtocolUtils.getSubArray2(data, offset, 4));
                //return new BigDecimal(new String(tmp));
            }

            case 7: { // int24
                return BigDecimal.valueOf(getExtendedLong(data,offset,3,dataOrder));
            }

            case 8: { // int32
                return BigDecimal.valueOf(getExtendedLong(data,offset,4,dataOrder));
            }

            case 9: { // int40
                return BigDecimal.valueOf(getExtendedLong(data,offset,5,dataOrder));
            }

            case 10: { // int48
                return BigDecimal.valueOf(getExtendedLong(data,offset,6,dataOrder));
            }

            case 11: { // int64
                return BigDecimal.valueOf(getLong(data,offset,8,dataOrder));
            }

            default:
                throw new IOException("C12ParseUtils, getNumberFromNonInteger, invalid non integer format, "+niFormat+" ! Cannot continue!");
        }
    }

    public static int getInt(byte[] data, int offset) throws IOException {
        return ProtocolUtils.getInt(data, offset, 1);
    }

    public static int getInt(byte[] data, int offset, int length, int dataOrder) throws IOException {
        if (dataOrder == 1) {
            return ProtocolUtils.getInt(data, offset, length);
        }
        else {
            return ProtocolUtils.getIntLE(data, offset, length);
        }
    }

    public static long getLong(byte[] data, int offset, int length, int dataOrder) throws IOException {
        if (dataOrder == 1) {
            return ProtocolUtils.getLong(data, offset, length);
        }
        else {
            return ProtocolUtils.getLongLE(data, offset, length);
        }
    }

    public static long getExtendedLong(byte[] data, int offset) throws IOException {
        return ProtocolUtils.getExtendedLong(data, offset, 1);
    }
    public static long getExtendedLong(byte[] data, int offset, int length, int dataOrder) throws IOException {
        if (dataOrder == 1) {
            return ProtocolUtils.getExtendedLong(data, offset, length);
        }
        else {
            byte[] reversedData = new byte[length];
            for (int i=0;i<length;i++) {
                reversedData[(length - 1) - i] = data[i + offset];
            }
            return ProtocolUtils.getExtendedLong(reversedData, 0, length);
        }
    }


    public static BigDecimal getBCD2BigDecimal(byte[] data, int offset, int length, int dataOrder) throws IOException {
        if (dataOrder == 1) {
            return doGetBCD2Long(data, offset, length);
        }
        else {
            return doGetBCD2LongLE(data, offset, length);
        }
    }

    public static long getBCD2Long(byte[] data, int offset, int length, int dataOrder) throws IOException {
        if (dataOrder == 1) {
            return doGetBCD2Long(data, offset, length).longValue();
        }
        else {
            return doGetBCD2LongLE(data, offset, length).longValue();
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
    private static BigDecimal doGetBCD2Long(byte[] byteBuffer,int offset, int length) throws IOException {
        long val=0;
        long multiplier=1;
        int decimalpoint=0;
        int nibbleCount=0;
        try {
            for(int i = ((offset+length)-1); i >= offset ; i-- ) {

                int msn = getC1219Val(ProtocolUtils.byte2int(byteBuffer[i]) >> 4);
                nibbleCount++;
                if (msn==13) {
                    decimalpoint = nibbleCount;
                }

                int lsn = getC1219Val(ProtocolUtils.byte2int(byteBuffer[i])& 0x0F);
                nibbleCount++;
                if (lsn==13) {
                    decimalpoint = nibbleCount;
                }

                if ((lsn!=13) && (lsn!=10)) {
                    val+=(lsn*multiplier);
                    multiplier*=10;
                }
                else if (lsn==10) {
                    val *= -1;
                }

                if ((msn!=13) && (msn!=10)) {
                    val+=(msn*multiplier);
                    multiplier*=10;
                }
                else if (msn==10) {
                    val *= -1;
                }
            }
            BigDecimal bd = BigDecimal.valueOf(val);
            bd=bd.movePointLeft(decimalpoint);
            return bd;
        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new IOException("ProtocolUtils, getBCD2IntLE, ArrayIndexOutOfBoundsException, "+e.getMessage());
        }
    }



    /**
     * Extract a int value from the BCD byte array starting at offset for length. The byte array is in little endial.
     * @return int value
     * @param byteBuffer byte array
     * @param offset 0-based offset in byte array
     * @param length number of bytes to use for calculation (min 1, max 4)
     * @throws IOException thrown when an exception happens
     */
    private static BigDecimal doGetBCD2LongLE(byte[] byteBuffer,int offset, int length) throws IOException {
        long val=0;
        long multiplier=1;
        int decimalpoint=0;
        int nibbleCount=0;

        try {
            for(int i = offset; i < (offset+length) ; i++ ) {
                 int msn = getC1219Val(ProtocolUtils.byte2int(byteBuffer[i]) >> 4);
                nibbleCount++;
                if (msn==13) {
                    decimalpoint = nibbleCount;
                }

                int lsn = getC1219Val(ProtocolUtils.byte2int(byteBuffer[i])& 0x0F);
                nibbleCount++;
                if (lsn==13) {
                    decimalpoint = nibbleCount;
                }

                if ((lsn!=13) && (lsn!=10)) {
                    val+=(lsn*multiplier);
                    multiplier*=10;
                }
                else if (lsn==10) {
                    val *= -1;
                }

                if ((msn!=13) && (msn!=10)) {
                    val+=(msn*multiplier);
                    multiplier*=10;
                }
                else if (msn==10) {
                    val *= -1;
                }
            }
            BigDecimal bd = BigDecimal.valueOf(val);
            bd=bd.movePointLeft(decimalpoint);
            return bd;

        }
        catch(ArrayIndexOutOfBoundsException e) {
            throw new IOException("ProtocolUtils, getBCD2IntLE, ArrayIndexOutOfBoundsException, "+e.getMessage());
        }
    }

    private static int getC1219Val(int val) {
        if ((val>=0) && (val<=9)) {
            return val;
        }
        else if (val==10) {
            return 10;
        }
        else if (val==11) {
            return 0;
        }
        else if (val==13) {
            return 13;
        }
        else {
            return 0;
        }
    }

}