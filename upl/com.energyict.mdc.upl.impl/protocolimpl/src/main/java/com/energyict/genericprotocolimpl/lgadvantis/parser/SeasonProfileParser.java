package com.energyict.genericprotocolimpl.lgadvantis.parser;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.genericprotocolimpl.lgadvantis.CosemFactory;
import com.energyict.genericprotocolimpl.lgadvantis.Task;
import com.energyict.protocolimpl.edf.messages.objects.SeasonProfile;

import java.io.IOException;

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
