package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.*;

public class CalendarNameParser extends AbstractParser implements Parser {

    private int calendar;
    
    public CalendarNameParser( int calendar ){
        this.calendar = calendar;
    }
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        byte value = dataType.getOctetString().getOctetStr()[0];
        
        if( CosemFactory.ACTIVE_CALENDAR == calendar ) {
            task.getActivityCalendar().setActiveCalendarName(value);
        } else {
            task.getActivityCalendar().setPassiveCalendarName(value);
        }
  
    }
    
}
