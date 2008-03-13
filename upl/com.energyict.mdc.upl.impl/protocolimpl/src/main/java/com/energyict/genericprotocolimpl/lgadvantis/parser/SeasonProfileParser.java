package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;


import com.energyict.edf.messages.objects.SeasonProfile;
import com.energyict.genericprotocolimpl.lgadvantis.*;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.OctetString;

public class SeasonProfileParser extends AbstractParser implements Parser {
    
    private int calendar;
    
    public SeasonProfileParser( int calendar ){
        this.calendar = calendar;
    }
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        Array array = (Array)dataType;
        
        for( int i = 0; i < array.nrOfDataTypes(); i ++ ) {
            
            Structure struct = (Structure)array.getDataType(i);
            
            byte name       = ( (OctetString) struct.getDataType(0) ).getOctetStr()[0];
            byte [] start   = ( (OctetString) struct.getDataType(1) ).getOctetStr();
            byte week       = ( (OctetString) struct.getDataType(2) ).getOctetStr()[0];
            
            SeasonProfile seasonProfile = new SeasonProfile( name, start, week );
            
            if( CosemFactory.ACTIVE_CALENDAR == calendar ) {
                task.getActivityCalendar().addActiveSeasonProfiles(seasonProfile);
            } else {
                task.getActivityCalendar().addPassiveSeasonProfiles(seasonProfile);
            }
            
        }
        
    }
    
}
