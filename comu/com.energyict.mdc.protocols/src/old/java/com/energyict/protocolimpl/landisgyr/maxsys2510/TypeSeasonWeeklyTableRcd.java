/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/** SEASON_WEEKLY_TABLE is the record which describes which TOU_DAILY_SCHEDS
 * to use during each DAY_OF_WK of each season.
 * 
 * @author fbo
 */

class TypeSeasonWeeklyTableRcd {
    
    ArrayList seasonWkly;

    static TypeSeasonWeeklyTableRcd parse( Assembly assembly ) throws IOException{
        TypeSeasonWeeklyTableRcd rcd = new TypeSeasonWeeklyTableRcd();
        int maxSeasons = assembly.getMaxSys().getTable0().getTypeMaximumValues().getMaxSeasons();
        rcd.seasonWkly = new ArrayList();
        
        for( int i = 0; i < maxSeasons; i ++ )
            rcd.seasonWkly.add( TypeWeeklyScheduleRcd.parse( assembly ));
            
        return rcd;
    }
    
    ArrayList getSeasonWkly() {
        return seasonWkly;
    }
    
    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "TypeSeasonWeeklyTable [ \n" );
        Iterator i = seasonWkly.iterator();
        while( i.hasNext())
            rslt.append( i.next().toString() + "\n" );
        rslt.append( "]" );
        return rslt.toString();
    }
    
}
