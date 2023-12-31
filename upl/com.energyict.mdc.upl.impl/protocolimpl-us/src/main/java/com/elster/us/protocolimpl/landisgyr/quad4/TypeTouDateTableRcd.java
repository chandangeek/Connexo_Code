package com.elster.us.protocolimpl.landisgyr.quad4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

class TypeTouDateTableRcd {

    ArrayList eventDates;

    static TypeTouDateTableRcd parse( Assembly assembly ) throws IOException{
        TypeTouDateTableRcd rcd = new TypeTouDateTableRcd();
        int maxDateEvents = assembly.getQuad4().getTable0().getTypeMaximumValues().getMaxDateEvents();
        rcd.eventDates = new ArrayList();
        
        for( int i = 0; i < maxDateEvents; i ++ )
            rcd.eventDates.add( TypeDateEvent.parse( assembly ) );
        
        return rcd;
    }
    
    ArrayList getEventDates() {
        return eventDates;
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "TypeTouDateTableRcd [ \n" );
        Iterator i = eventDates.iterator();
        while( i.hasNext() ){
            rslt.append( i.next().toString() + "\n" );
        }
        rslt.append( "]" );
        return rslt.toString();
    }
    
}
