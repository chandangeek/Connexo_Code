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
            
            struct.addDataType( OctetString.fromByteArray( new byte [] { sp.getName() } ) );
            struct.addDataType( OctetString.fromByteArray( sp.getStart().getOctetString().getOctets() ) );
            struct.addDataType( OctetString.fromByteArray( new byte [] { sp.getWeek() } ) );
            
        }
        
        return array;
         
    }
    
}
