package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocolimpl.edf.messages.objects.ActivityCalendar;

public class ActivatePassiveCalendarEncoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
        
        ActivityCalendar ac = (ActivityCalendar) value;
        
        return 
            new OctetString( 
                    ac.getActivatePassiveCalendarTime()
                        .getOctetString().getOctets() );
        
        
    }
    
}
