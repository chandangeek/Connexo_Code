package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DataType {

    static int dbg = 0;

    TimeZone timeZone;

    final UTC utc = new UTC();
    final BCD bcd = new BCD();
    final StringType string = new StringType();
    final Time dateTime = new Time();

    public DataType( TimeZone timeZone ){
        this.timeZone = timeZone;
    }

    /**
     * UTC Timestamp seconds since 1970 in 4 bytes
     */
    class UTC {

        Calendar parse( byte [] ba, int offset ) throws IOException {

            long shift = ProtocolUtils.getIntLE(ba,offset,4)&0xFFFFFFFFL;
            Calendar c= null;
            if( shift != 0 ) {
                c = ProtocolUtils.getCalendar( timeZone, shift );
            }

            if( DataType.dbg > 0 ) {
                StringBuffer sb = new StringBuffer();
                DataType.toShortString( sb, ba, offset );
                System.out.println( " UTC.parse() " +  sb.toString() + " " + ( c == null ? "<null>" : c.getTime().toString() ) );
            }

            return c;
        }
    }

    static class BCD {

        long parse( byte [] ba, int offset ) throws IOException {
            return parse( ba, offset, 4 );
        }

        long parse( byte [] ba, int offset, int length ) throws IOException {
            return Long.parseLong( Long.toHexString(
                    ProtocolUtils.getLong(ba,offset,length)));
        }

        Quantity toQuantity( byte [] ba, int offset, int length, Unit unit )
                                                          throws IOException{
            long l = parse( ba, offset, length );
            return new Quantity( new Long(l), unit );
        }


    }

    static class StringType {

        String parse(byte[] ba, int offset, int length ) {
            byte [] pi = new byte[length];
            System.arraycopy( ba, offset, pi, 0, length );
            return new String( pi );
        }

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
        if (r.length() < 2)
            r = "0" + r;
        return r;
    }

    /** Special debug stuff */
    public static String toHexaString( byte [] b, int pos, int length ){
        StringBuffer sb = new StringBuffer();
        if( b == null )
            sb.append( " <null> " );
        else
            for( int i = pos; i < pos + length; i ++  ){
                sb.append( "0x" + toHexaString( b[i] ) + " " );
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
            if( i == anIndex ) sb.append( "^" );
            if( i < 0 || i >= ba.length )
                sb.append( ".. " );
            else
                sb.append( toHexaString( ba[i] ) + " " );
        }

        return sb;
    }

}
