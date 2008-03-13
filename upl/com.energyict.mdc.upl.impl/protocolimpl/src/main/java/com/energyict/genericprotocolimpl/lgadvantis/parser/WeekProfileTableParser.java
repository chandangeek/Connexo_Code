package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.*;

import com.energyict.edf.messages.objects.WeekProfile;
import com.energyict.genericprotocolimpl.lgadvantis.*;

public class WeekProfileTableParser extends AbstractParser implements Parser {

    private int calendar;
    
    public WeekProfileTableParser(int calendar) {
        this.calendar = calendar;
    }
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
    
        Array array = (Array)dataType;
        
        for( int i = 0; i < array.nrOfDataTypes(); i ++ ) {
            
            Structure struct = (Structure)array.getDataType(i);
            
            byte wpn        = ( (OctetString) struct.getDataType(0) ).getOctetStr()[0];
            int monday      = struct.getDataType(1).intValue();
            int tuesday     = struct.getDataType(2).intValue();
            int wednesday   = struct.getDataType(3).intValue();
            int thursday    = struct.getDataType(4).intValue();
            int friday      = struct.getDataType(5).intValue();
            int saturday    = struct.getDataType(6).intValue();
            int sunday      = struct.getDataType(7).intValue();
        
            
            WeekProfile wp = new WeekProfile( "" +wpn );
            
            wp.setMonday(monday);
            wp.setTuesday(tuesday);
            wp.setWednesday(wednesday);
            wp.setThursday(thursday);
            wp.setFriday(friday);
            wp.setSaturday(saturday);
            wp.setSunday(sunday);
            
            if( CosemFactory.ACTIVE_CALENDAR == calendar ) {
                task.getActivityCalendar().addActiveWeekProfiles(wp);
            } else {
                task.getActivityCalendar().addPassiveWeekProfiles(wp);
            }
            
        }

    }
    
}
