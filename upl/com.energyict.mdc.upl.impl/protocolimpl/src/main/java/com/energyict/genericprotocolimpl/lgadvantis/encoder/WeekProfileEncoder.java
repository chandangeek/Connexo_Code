package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import java.util.Iterator;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.edf.messages.objects.ActivityCalendar;
import com.energyict.edf.messages.objects.WeekProfile;

public class WeekProfileEncoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
        
        ActivityCalendar ac = (ActivityCalendar) value;
        Array array = new Array();
        
        
        Iterator i = ac.getPassiveWeekProfiles().iterator();
        while( i.hasNext() ) {
            WeekProfile week = (WeekProfile) i.next();
        
            Structure struct = new Structure( );
            array.addDataType(struct);
            
            struct.addDataType( new OctetString( new byte[] { week.getName() } ) );
            
            struct.addDataType( new Unsigned8( week.getMonday()     ) );
            struct.addDataType( new Unsigned8( week.getTuesday()    ) );
            struct.addDataType( new Unsigned8( week.getWednesday()  ) );
            struct.addDataType( new Unsigned8( week.getThursday()   ) );
            struct.addDataType( new Unsigned8( week.getFriday()     ) );
            struct.addDataType( new Unsigned8( week.getSaturday()   ) );
            struct.addDataType( new Unsigned8( week.getSunday()     ) );
            
        }
        
        return array; 
       
        
    }
    
}
