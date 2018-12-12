/*
 * InformationObject19.java
 *
 * Created on 12 april 2006, 7:56
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Map;
import java.util.TreeMap;

/** This class maintains 2 maps:
 * - using object id as key
 * - using obis code e field as key
 * 
 * @author fbo */

public class InformationObject87  extends InformationObject {
    
    Map objectIdMap = new TreeMap();
    Map eFieldMap = new TreeMap();
    
    /** Creates a new instance of InformationObject19 */
    public InformationObject87() { }
    
    InformationObject87Period getPeriod( int id ) {
        return (InformationObject87Period)eFieldMap.get( new Integer(id) );
    }
    
    InformationObject87 add( InformationObject87Period period ){
        int objectId = period.getInfoAddress();
        
        if( objectId < 20 || objectId > 29 )
            throw new ParseException( "Object id not supported: " + objectId );
        
        objectIdMap.put( new Integer( period.getInfoAddress()), period);
        eFieldMap.put( new Integer( period.getInfoAddress()-20), period);
        return this;
    }
    
}
