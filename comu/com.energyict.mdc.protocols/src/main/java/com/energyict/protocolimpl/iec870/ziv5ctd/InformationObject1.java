/*
 * InformationObject1.java
 *
 * Created on 17 april 2006, 11:52
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/** A collection of events.
 * @author fbo */

public class InformationObject1 extends InformationObject {
    
    Map meterEvents = new TreeMap();
    
    /** Creates a new instance of InformationObject1 */
    InformationObject1() { }
    
    InformationObject1 add( InformationObject1Event event ) {
        if( event.toMeterEvent() != null )
            meterEvents.put( event.getDate(), event.toMeterEvent() );
        return this;
    }
    
    Collection getMeterEvents( ){
        return meterEvents.values();
    }
    
    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "InformationObject1 [\n");
        Iterator i = meterEvents.keySet().iterator();
        while( i.hasNext() ) {
            Object key = i.next();
            rslt.append( key + " " + meterEvents.get(key) );
        }
        rslt.append( "\n]");
        return rslt.toString();
    }
    
}
