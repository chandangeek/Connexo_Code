package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocolimpl.edf.messages.objects.ActivityCalendar;

public class CalendarNameEncoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
        
        ActivityCalendar ac = (ActivityCalendar) value;
        
        byte [] ba = new byte [] { ac.getPassiveCalendarName() };
        
        return OctetString.fromByteArray( ba );
        
    } 
    
}
