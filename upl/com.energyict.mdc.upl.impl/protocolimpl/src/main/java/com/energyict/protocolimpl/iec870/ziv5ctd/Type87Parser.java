/*
 * Type19Parser.java
 *
 * Created on 12 april 2006, 8:13
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.math.BigDecimal;
import java.util.TimeZone;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

/** @author fbo */

public class Type87Parser implements TypeParser {
    
    TimeZone timeZone;
    
    /** Creates a new instance of Type19Parser */
    public Type87Parser(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    public InformationObject parse(ByteArray byteArray) {
        Unit a = Unit.get( "kWh" );
        Unit r = Unit.get( "varh" );
        Unit d = Unit.get( "kW" );
        
        ByteArray byteArray87 = byteArray.sub( 7, byteArray.length()-8 );
        
        InformationObject87Period io = new InformationObject87Period();
        io.setInfoAddress( byteArray.intValue(6, 1) );
        
        io.setAbsoluteA( wrap( byteArray87.sub( 0, 4 ).intValue(0, 4), a) );
        io.setIncrementalA( wrap( byteArray87.sub( 4, 4 ).intValue(0, 4), a));
        
        io.setAbsoluteRi( wrap( byteArray87.sub( 9, 4 ).intValue(0, 4), r) );
        io.setIncrementalRi( wrap( byteArray87.sub( 13, 4 ).intValue(0, 4), r) );
        
        io.setAbsoluteRc(wrap( byteArray87.sub( 18, 4 ).intValue(0, 4), r));
        io.setIncrementalRc(wrap( byteArray87.sub( 22, 4 ).intValue(0, 4), r));
        
        io.setReserva7(wrap( byteArray87.sub( 28, 4 ).intValue(0, 4), r));
        io.setReserva8(wrap( byteArray87.sub( 33, 4 ).intValue(0, 4), r));
        
        io.setMaxPotentia( wrap( byteArray87.sub( 37, 4 ).intValue(0, 4), d) );
        io.setMaxPotentiaDate( new CP40Time(timeZone, byteArray87.sub(41, 5) ).getDate() );
        
        io.setStartPeriod( new CP40Time(timeZone, byteArray87.sub(52, 5) ).getDate() );
        io.setEndPeriod( new CP40Time(timeZone, byteArray87.sub(57, 5) ).getDate() );
        
        return (InformationObject)io;
    }
    
    Quantity wrap(int value, Unit unit) {
        return new Quantity( new BigDecimal(value), unit );
    }
    
    public static void main( String [] args ) {
        ByteArray byteArray = new ByteArray(
                new byte [] {
            (byte)0x48, (byte)0x05, (byte)0x00, (byte)0x00,
            (byte)0x48, (byte)0x05, (byte)0x00, (byte)0x00, (byte)0xd0,
            
            (byte)0xe6, (byte)0x04, (byte)0x00, (byte)0x00,
            (byte)0xe6, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0xd0,
            
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xd0,
            
            (byte)0x0a, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x40, (byte)0xd9, (byte)0xe5, (byte)0x00, (byte)0x00,
            
            (byte)0x40, (byte)0x06, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x0f, (byte)0x8b, (byte)0x2a, (byte)0x04,
            
            (byte)0x06, (byte)0xd0, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x80, (byte)0x03, (byte)0x87, (byte)0x24,
            (byte)0x06, (byte)0x60, (byte)0x25, (byte)0x8f, (byte)0x4b,
            (byte)0x04, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0xd0
        });
        
        Type87Parser parser = new Type87Parser( TimeZone.getDefault() );
        System.out.println( parser.parse(byteArray) );
        
        
    }
    
    
}
