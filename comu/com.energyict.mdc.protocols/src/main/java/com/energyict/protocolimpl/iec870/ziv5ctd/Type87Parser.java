/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Type19Parser.java
 *
 * Created on 12 april 2006, 8:13
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import java.math.BigDecimal;
import java.util.TimeZone;

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

        return io;
    }

    Quantity wrap(int value, Unit unit) {
        return new Quantity( new BigDecimal(value), unit );
    }

}
