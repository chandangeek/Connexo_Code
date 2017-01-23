/*
 * Type8Parser.java
 *
 * Created on 27 March 2006, 15:39
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.math.BigDecimal;
import java.util.TimeZone;

/** @author fbo */

public class Type8Parser implements TypeParser {

    /** Creates a new instance of Type8Parser */
    public Type8Parser() { }

    public InformationObject parse(ByteArray byteArray) {

        ByteArray vBArray = byteArray.sub(6);
        InformationObject8 i8 = new InformationObject8( );

        CP40Time t = new CP40Time(TimeZone.getDefault(), byteArray.sub( 54, 5 ) );
        i8.setDate( t.getDate() );
        i8.setQuanitity1( toQuantity( vBArray.intValue(1, 4), Unit.get("kWh") ) );
        i8.setStatus1(vBArray.intValue(5, 1));
        i8.setQuanitity2( toQuantity( vBArray.intValue(7, 4), Unit.get("kWh") ) );
        i8.setStatus2(vBArray.intValue(11, 1));
        i8.setQuanitity3( toQuantity( vBArray.intValue(13, 4), Unit.get("kVArh") ) );
        i8.setStatus3(vBArray.intValue(17, 1));
        i8.setQuanitity4( toQuantity( vBArray.intValue(19, 4), Unit.get("kVArh") ) );
        i8.setStatus4(vBArray.intValue(23, 1));
        i8.setQuanitity5( toQuantity( vBArray.intValue(25, 4), Unit.get("kVArh") ) );
        i8.setStatus5(vBArray.intValue(29, 1));
        i8.setQuanitity6( toQuantity( vBArray.intValue(31, 4), Unit.get("kVAar") ) );
        i8.setStatus6(vBArray.intValue(35, 1));
        return (InformationObject)i8;

    }

    Quantity toQuantity( int v, Unit u ){
        return new Quantity( new BigDecimal(v), u);
    }

}
