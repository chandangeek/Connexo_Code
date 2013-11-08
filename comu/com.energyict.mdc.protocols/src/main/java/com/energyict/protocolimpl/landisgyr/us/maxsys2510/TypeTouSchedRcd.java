package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;

class TypeTouSchedRcd {
    
    TypeTouDateTableRcd dateSchedule;
    TypeSeasonWeeklyTableRcd seasonSchedule;
    TypeDailySchedTableRcd dailySchedule;
    TypeCyclicRateReset cyclicReset;

    static TypeTouSchedRcd parse( Assembly assembly ) throws IOException{
        TypeTouSchedRcd rcd = new TypeTouSchedRcd();
        rcd.dateSchedule = TypeTouDateTableRcd.parse(assembly);
        rcd.seasonSchedule = TypeSeasonWeeklyTableRcd.parse(assembly);
        rcd.dailySchedule = TypeDailySchedTableRcd.parse(assembly);
        rcd.cyclicReset = TypeCyclicRateReset.parse(assembly);
        return rcd;
    }
    
    TypeCyclicRateReset getCyclicReset() {
        return cyclicReset;
    }

    TypeDailySchedTableRcd getDailySchedule() {
        return dailySchedule;
    }

    TypeTouDateTableRcd getDateSchedule() {
        return dateSchedule;
    }

    TypeSeasonWeeklyTableRcd getSeasonSchedule() {
        return seasonSchedule;
    }
    
    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "TypeTouScheRcd [ \n" );
        rslt.append( dateSchedule.toString() + "\n" );
        rslt.append( seasonSchedule.toString() + "\n" );
        rslt.append( dailySchedule.toString() + "\n" );
        rslt.append( cyclicReset.toString() + "\n" );
        rslt.append( "]" );
        return rslt.toString();
    }
}
