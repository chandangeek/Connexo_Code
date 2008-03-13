package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.edf.messages.objects.DayProfile;
import com.energyict.edf.messages.objects.DayProfileSegment;
import com.energyict.genericprotocolimpl.lgadvantis.*;

public class DayProfileParser extends AbstractParser implements Parser {
    
    private int calendar;
    
    public DayProfileParser(int calendar) {
        this.calendar = calendar;
    }
    
    public void parse(AbstractDataType dataType, Task task) throws IOException {
        
        Array array = (Array) dataType;
        
        for (int i = 0; i < array.nrOfDataTypes(); i++) {
            
            Structure struct = (Structure) array.getDataType(i);
            
            int dayId = struct.getDataType(0).intValue();
            
            DayProfile dp = new DayProfile( dayId );
            
            Array array2 = (Array) struct.getDataType(1);
            
            for (int i2 = 0; i2 < array2.nrOfDataTypes(); i2++) {
                
                Structure struct2 = array2.getDataType(0).getStructure();
                
                byte[] start = ((OctetString) struct2.getDataType(0)).getOctetStr();
                byte[] script = ((OctetString) struct2.getDataType(1)).getOctetStr();
                int select = struct2.getDataType(2).intValue();
                
                new DayProfileSegment(start, script, select);
            }
            
            if( CosemFactory.ACTIVE_CALENDAR == calendar ) {
                task.getActivityCalendar().addActiveDayProfiles(dp);
            } else {
                task.getActivityCalendar().addPassiveDayProfiles(dp);
            }
            
        }
        
        
        
        
    }
    
}
