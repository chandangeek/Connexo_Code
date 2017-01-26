/*
 * CP40Time.java
 *
 * Created on 28 March 2006, 10:02
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 7 octet timestamp
 * @author fbo
 */

class CP56Time extends Time {
    

    int millisecond;
    int second;
    
    /** Creates a new instance of CP40Time */
    CP56Time(TimeZone timeZone, Date date) {
        Calendar c = Calendar.getInstance(timeZone); 
        c.setTime( date );
        second = c.get( Calendar.SECOND );
        minute = c.get( Calendar.MINUTE );
        tarif = 0;
        invalid = 0;
        hour = c.get( Calendar.HOUR_OF_DAY );
        summerTime = ( c.get(Calendar.DST_OFFSET) > 0 ) ? 1 : 0; 
        dayOfMonth = c.get( Calendar.DAY_OF_MONTH );
        dayOfWeek = toZivDayIndex(c);
        month = toZivMonth(c.get( Calendar.MONTH ));
        eti = 0;
        pti = 0;
        year = toZivYear(c.get( Calendar.YEAR ));
    
    }
    
    CP56Time(TimeZone timeZone, ByteArray byteArray) {
        this.timeZone = timeZone;
        parse(byteArray);
    }
    
    Date getDate( ){
        Calendar c = ProtocolUtils.getCalendar( timeZone ); 
        c.clear();
        c.set( toCalendarYear(year), toCalendarMonth(month), dayOfMonth, hour, minute, second );
        c.set(Calendar.MILLISECOND, millisecond );
        return c.getTime();
    }
    
    private void parse( ByteArray byteArray ){
        
        millisecond = byteArray.bitValue(0, 9);
        second = byteArray.bitValue(10, 15);
        minute = byteArray.bitValue(16, 21);
        tarif = byteArray.bitValue(22,22);
        invalid = byteArray.bitValue(23,23);
        hour = byteArray.bitValue(24,28);
        summerTime = byteArray.bitValue(31,31);
        dayOfMonth = byteArray.bitValue(32, 36);
        dayOfWeek = byteArray.bitValue(37,39);
        
        month = byteArray.bitValue(40, 43);
        eti = byteArray.bitValue(44, 45);
        pti = byteArray.bitValue(46, 47);
        
        year = byteArray.bitValue(48, 54);
        
    }
    
     public ByteArray toByteArray( ) {
        
        StringBuffer sb = new StringBuffer();
        // millisecs are not set
        sb.append( "00000000" );
        
        sb.append( toBitString(second, 6));
        sb.append( "00" );
        
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
        
        sb.append( "00" );
        sb.append( toBitString( year, 6 ));
        
        //System.out.println( sb.toString() );
        byte [] r = new byte[] { 0, 0, 0, 0, 0, 0, 0 };
        String s = sb.toString();
            
        for( int i = 0; i < s.length(); i+=8 ) {
            r[i/8] =(byte)Integer.parseInt( s.substring(i, i+8), 2 );
        }
        
        return new ByteArray(r);
    }
    
    String toBitString(int value, int nrBits ){
        String binary = Integer.toBinaryString( value );
        for( int i = binary.length(); i < nrBits; i ++ )
            binary = "0" + binary;
        return new StringBuffer(binary).toString();
    }
    
    public static void main(String args[]){
                
        //ByteArray x1 = new ByteArray( new byte[] { 0x7b, 0x15, (byte)0x20, (byte)0x91, (byte)0x65, 0x04, 0x06 } );
/*        ByteArray x1 = new ByteArray( new byte[] { (byte)0xba, (byte)0x45, (byte)0x34, (byte)0x88, (byte)0x86, 0x04, 0x06 } );
        System.out.println( x1.toBinaryString() );
        
        CP56Time cpTime = new CP56Time( TimeZone.getDefault(), x1 );
        System.out.println( cpTime );
        System.out.println( cpTime.getDate() );
    
        ByteArray x2 = cpTime.toByteArray();
        System.out.println( "x2 " + x2 );
        cpTime = new CP56Time( TimeZone.getDefault(), x1 );
        System.out.println(cpTime.getDate());
        
        cpTime = new CP56Time( TimeZone.getDefault(), new Date() );
        System.out.println( cpTime.toByteArray().toBinaryString() );
        System.out.println( cpTime.toByteArray().toHexaString() );
        cpTime =  new CP56Time( TimeZone.getDefault(), cpTime.toByteArray() );
        System.out.println( cpTime.getDate() );
        
       */ 
        ByteArray x1 = new ByteArray( new byte[] { 0x55, 0x06, (byte)0x04, (byte)0x8a, (byte)0x4b, 0x04, 0x06 } );
        /*
        Date d = new Date();
        CP56Time cpTime1 = new CP56Time( TimeZone.getDefault(), x1 );        
        //CP56Time cpTime2 = new CP56Time( TimeZone.getDefault(), cpTime1.toByteArray() );
        
        System.out.println( cpTime1.toByteArray() + " " +  d );
        //System.out.println( /*cpTime2.toByteArray() + " " +  cpTime2.getDate() );
 *
        */
        
        Calendar c = Calendar.getInstance();
        c.set( 2006, 03, 11, 10, 04, 01 );
        
        System.out.println(c.getTime());
        CP56Time time = new CP56Time( TimeZone.getDefault(), c.getTime() );
        System.out.println( x1 );
        System.out.println( time.toByteArray() );
        
    }
    
}
