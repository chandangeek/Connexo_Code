/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DataType {

    static int dbg = 0;

    TimeZone timeZone;

    final Time dateTime = new Time();
    final IntegrationPeriod integrationPeriod = new IntegrationPeriod ();

    public DataType( TimeZone timeZone ){
        this.timeZone = timeZone;
    }

    class Time {

        Date parse( byte [] data ) throws IOException {
            Calendar calendar = ProtocolUtils.getCalendar(timeZone);
            calendar.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[0]));
            calendar.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[1]));
            calendar.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[2]));
            calendar.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex((byte)((int)data[3]&0x3F)));
            calendar.set(Calendar.MONTH,ProtocolUtils.BCD2hex((byte)((int)data[4]&0x1F))-1);
            int y = ProtocolUtils.BCD2hex(data[6]);
            calendar.set(Calendar.YEAR,y == 99 ? 1999 : y+2000);

            return calendar.getTime();
        }

        byte[] construct( Date date ) {
            Calendar calendar = ProtocolUtils.getCalendar(timeZone);
            calendar.clear();
            calendar.setTime(date);
            byte[] data = new byte[14];

            ProtocolUtils.val2BCDascii(calendar.get(Calendar.SECOND),data,0);
            ProtocolUtils.val2BCDascii(calendar.get(Calendar.MINUTE),data,2);
            ProtocolUtils.val2BCDascii(calendar.get(Calendar.HOUR_OF_DAY),data,4);
            ProtocolUtils.val2BCDascii(calendar.get(Calendar.DAY_OF_MONTH),data,6);
            ProtocolUtils.val2BCDascii(calendar.get(Calendar.MONTH)+1,data,8);
            ProtocolUtils.val2BCDascii(0,data,10);
            ProtocolUtils.val2BCDascii(calendar.get(Calendar.YEAR)-2000,data,12);

            return data;
        }

    }

    class IntegrationPeriod {

        int parse( byte data ) {
            if( data == 0x00 ){
            	return 1 * 60;
            }
            if( data == 0x01 ) {
				return 2 * 60;
			}
            if( data == 0x02 ) {
				return 3 * 60;
			}
            if( data == 0x03 ) {
				return 4 * 60;
			}
            if( data == 0x04 ) {
				return 5 * 60;
			}
            if( data == 0x05 ) {
				return 6 * 60;
			}
            if( data == 0x06 ) {
				return 10 * 60;
			}
            if( data == 0x07 ) {
				return 15 * 60;
			}
            if( data == 0x08 ) {
				return 20 * 60;
			}
            if( data == 0x09 ) {
				return 30 * 60;
			}
            if( data == 0x0A ) {
				return 60 * 60;
			}
            return -1;
        }

    }

    /** Special debug stuff */
    public static String toBinaryString(int b) {
        char[] result = {'0', '0', '0', '0', '0', '0', '0', '0'};
        char[] bitArray = Integer.toBinaryString(b & 0xFF).toCharArray();
        System.arraycopy(bitArray, 0, result, 8 - bitArray.length,
                bitArray.length);
        return new String(result);
    }

    /** Special debug stuff */
    public static String toHexaString(int b) {
        String r = Integer.toHexString(b & 0xFF);
        if (r.length() < 2) {
			r = "0" + r;
		}
        return r;
    }

    /** Special debug stuff */
    public static String toHexaString( byte [] b, int pos, int length ){
        StringBuffer sb = new StringBuffer();
        if( b == null ) {
			sb.append( " <null> " );
		} else {
			for( int i = pos; i < pos + length; i ++  ){
                sb.append( "0x" + toHexaString( b[i] ) + " " );
            }
		}
        return sb.toString();
    }

    /** Special debug stuff */
    public static String toHexaString( byte [] b ){
        return toHexaString( b, 0, b.length );
    }

    /** Special debug stuff */
    public static StringBuffer toShortString( StringBuffer sb, byte [] ba, int anIndex ){
        int start = ( anIndex  - 8 );
        int stop = ( anIndex  + 8 );

        for( int i = start; i < stop; i ++ ) {
            if( i == anIndex ) {
				sb.append( "^" );
			}
            if( i < 0 || i >= ba.length ) {
				sb.append( ".. " );
			} else {
				sb.append( toHexaString( ba[i] ) + " " );
			}
        }

        return sb;
    }

}
