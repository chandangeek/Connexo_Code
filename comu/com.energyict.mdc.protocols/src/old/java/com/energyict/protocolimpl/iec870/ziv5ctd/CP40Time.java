/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CP40Time.java
 *
 * Created on 28 March 2006, 10:02
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.protocols.util.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 5 octet timestamp
 * @author fbo
 */

class CP40Time extends Time {

    /** Creates a new instance of CP40Time */
    CP40Time(TimeZone timeZone, Date date) {
        Calendar c = Calendar.getInstance(timeZone);
        c.setTime( date );

        minute = c.get( Calendar.MINUTE );
        tarif = 0;
        invalid = 0;
        hour = c.get( Calendar.HOUR_OF_DAY );
        summerTime = ( c.get(Calendar.DST_OFFSET) > 0 ) ? 1 : 0;
        dayOfMonth = c.get( Calendar.DAY_OF_MONTH );
        dayOfWeek = toZivDayIndex(c);
        month = toZivMonth( c.get( Calendar.MONTH ) );
        eti = 0;
        pti = 0;
        year = toZivYear(c.get(Calendar.YEAR));

    }

    CP40Time(TimeZone timeZone, ByteArray byteArray) {
        this.timeZone = timeZone;
        parse(byteArray);
    }

    Date getDate( ){
        Calendar c = ProtocolUtils.getCalendar( timeZone );
        c.clear();
        c.set( toCalendarYear(year), toCalendarMonth(month), dayOfMonth, hour, minute );
        return c.getTime();
    }

    private void parse( ByteArray byteArray ){

        minute = byteArray.bitValue(0, 5);

        tarif = byteArray.bitValue(6,6);
        invalid = byteArray.bitValue(7,7);

        hour = byteArray.bitValue(8,12);
        //reserve
        summerTime = byteArray.bitValue(15,15);
        dayOfMonth = byteArray.bitValue(16, 20);
        dayOfWeek = byteArray.bitValue(21,23);
        month = byteArray.bitValue(24, 26);

        eti = byteArray.bitValue(28, 28);
        pti = byteArray.bitValue(30, 30);
        year = byteArray.bitValue(32, 38);

    }

    public ByteArray toByteArray( ) {

        StringBuilder sb = new StringBuilder();

        sb.append( toBitString( tarif, 1 ));
        sb.append( toBitString( invalid, 1 ));
        sb.append( toBitString( minute, 6 ));

        sb.append( toBitString( summerTime, 1 ));
        sb.append( "00" );
        sb.append( toBitString( hour, 5 ));

        sb.append( toBitString( dayOfWeek, 3 ));
        sb.append( toBitString( dayOfMonth, 5 ));

        sb.append( toBitString( pti, 2 ));
        sb.append( toBitString( eti, 2 ));
        sb.append( toBitString( month, 4 ));

        sb.append( "0" );
        sb.append( toBitString( year, 7 ));

        //System.out.println( sb.toString() );
        byte [] r = new byte[] { 0, 0, 0, 0, 0 };
        String s = sb.toString();

        for( int i = 0; i < s.length(); i+=8 ) {
            r[i/8] =(byte)Integer.parseInt( s.substring(i, i+8), 2 );
        }

        return new ByteArray(r);
    }

    String toBitString(int value, int nrBits ){
        String binary = Integer.toBinaryString( value );
        for( int i = binary.length(); i < nrBits; i ++ ) {
            binary = "0" + binary;
        }
        return binary;
     }

}