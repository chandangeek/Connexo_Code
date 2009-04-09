package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

class TypeDailySchedTableRcd {

    ArrayList daySchedules;
    
    static TypeDailySchedTableRcd parse( Assembly assembly ) throws IOException{
        TypeDailySchedTableRcd rcd = new TypeDailySchedTableRcd();
        int maxDailySched = assembly.getMaxSys().getTable0().getTypeMaximumValues().getMaxDailyScheds();
        rcd.daySchedules = new ArrayList();
        
        for (int i = 0; i < maxDailySched; i ++ ) 
            rcd.daySchedules.add( TypeDailySchedRcd.parse(assembly));
        
        return rcd;
    }
    
    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append( "TypeDailySchedTableRcd [ \n" );
        Iterator iter = daySchedules.iterator();
        while (iter.hasNext()) 
            rslt.append( iter.next().toString() + "\n" );
        rslt.append( "\n ]");
        return rslt.toString();
    }
    
}
