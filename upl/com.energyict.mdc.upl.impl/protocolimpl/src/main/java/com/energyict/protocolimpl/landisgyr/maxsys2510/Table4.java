package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.protocol.MeterEvent;

class Table4 {

    List histRcd = new ArrayList();
    List meterEvents = new ArrayList();
   
    static Table4 parse( MaxSys maxSys, Assembly assembly ){
        Table4 t = new Table4();
        
        for( int i = 0; i < 100; i ++ )
            t.add(TypeHistRcd.parse(assembly, maxSys.getTimeZone()));
        
        return t;
    }

    void add( TypeHistRcd typeHistRcd ){
        histRcd.add( typeHistRcd );
        MeterEvent me = typeHistRcd.toMeterEvent();
        if( me != null )
        meterEvents.add( me );
    }
    
    List getHistRcd() {
        return histRcd;
    }
    
    List getMeterEvents( ){
        return meterEvents;
    }
    
    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "Table4 [\n" );
        Iterator i = meterEvents.iterator();
        while( i.hasNext() ) { 
            MeterEvent me = (MeterEvent)i.next();
            rslt.append( "\t" + me.getTime() + " " + me  + " " +  me.getMessage() + "\n" );
        }
        rslt.append( "]" );
        return rslt.toString();
    }
}
