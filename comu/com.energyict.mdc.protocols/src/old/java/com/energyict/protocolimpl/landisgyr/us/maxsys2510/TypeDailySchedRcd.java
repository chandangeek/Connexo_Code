/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

class TypeDailySchedRcd {
    
    ArrayList timedEvents;
    
    static TypeDailySchedRcd parse( Assembly assembly ) throws IOException{
        TypeDailySchedRcd rcd = new TypeDailySchedRcd();
        int maxDailyEvents = assembly.getMaxSys().getTable0().getTypeMaximumValues().getMaxDailyEvents();
        rcd.timedEvents = new ArrayList();
        
        for (int i = 0; i < maxDailyEvents; i ++ ) 
            rcd.timedEvents.add( TypeEventTimeRcd.parse(assembly));
        
        return rcd;
    }
    
    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append( "TypeDailySchedTableRcd [ \n" );
        Iterator iter = timedEvents.iterator();
        while (iter.hasNext()) 
            rslt.append( iter.next().toString() + "\n" );
        rslt.append( "\n ]");
        return rslt.toString();
    }
}
 