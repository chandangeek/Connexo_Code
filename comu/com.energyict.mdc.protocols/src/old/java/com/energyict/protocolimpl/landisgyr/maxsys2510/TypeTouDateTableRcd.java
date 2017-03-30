/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

class TypeTouDateTableRcd {

    ArrayList eventDates;

    static TypeTouDateTableRcd parse( Assembly assembly ) throws IOException{
        TypeTouDateTableRcd rcd = new TypeTouDateTableRcd();
        int maxDateEvents = assembly.getMaxSys().getTable0().getTypeMaximumValues().getMaxDateEvents();
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
