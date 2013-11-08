package com.energyict.protocolimpl.landisgyr.us.maxsys2510;


/**
 * This structure defines which DAY_SCHEDULE to use for each day of the week
 * in this weekly sched.
 * 
 * @author fbo
 */

class TypeWeeklyScheduleRcd {
    
    int [] daysOfWk;

    static TypeWeeklyScheduleRcd parse( Assembly assembly ){
        TypeWeeklyScheduleRcd rcd = new TypeWeeklyScheduleRcd();
        rcd.daysOfWk = new int[7];
        rcd.daysOfWk[0] = assembly.wordValue();
        rcd.daysOfWk[1] = assembly.wordValue();
        rcd.daysOfWk[2] = assembly.wordValue();
        rcd.daysOfWk[3] = assembly.wordValue();
        rcd.daysOfWk[4] = assembly.wordValue();
        rcd.daysOfWk[5] = assembly.wordValue();
        rcd.daysOfWk[6] = assembly.wordValue();
        return rcd;
    }
    
    int[] getDaysOfWk() {
        return daysOfWk;
    }
    
    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "TypeWeeklyScheduleRcd [ " );
        for( int i = 0; i < daysOfWk.length; i ++ ){
            rslt.append( daysOfWk[i] + " " );
        }
        rslt.append( "]" );
        return rslt.toString();
    }
    
}
