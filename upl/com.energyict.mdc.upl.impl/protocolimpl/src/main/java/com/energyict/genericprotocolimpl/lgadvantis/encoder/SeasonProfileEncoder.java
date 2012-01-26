package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocolimpl.edf.messages.objects.ActivityCalendar;
import com.energyict.protocolimpl.edf.messages.objects.SeasonProfile;

import java.util.Iterator;

public class SeasonProfileEncoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
        
        Array array = new Array();
        ActivityCalendar ac = (ActivityCalendar) value;
        
        Iterator i = ac.getPassiveSeasonProfiles().iterator();
        while( i.hasNext() ) {
            SeasonProfile sp = (SeasonProfile) i.next();
            
            Structure struct = new Structure();
            array.addDataType(struct);
            
            struct.addDataType( new OctetString( new byte [] { sp.getName() } ) );
            struct.addDataType( new OctetString( sp.getStart().getOctetString().getOctets() ) );
            struct.addDataType( new OctetString( new byte [] { sp.getWeek() } ) );
            
        }
        
        return array;
         
    }
    
}
