package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import java.util.Iterator;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.edf.messages.objects.*;

public class DayProfileEncoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
       
        ActivityCalendar ac = (ActivityCalendar) value;
        Array array = new Array();
        
        Iterator i = ac.getPassiveDayProfiles().iterator();
        while( i.hasNext() ) {
            
            DayProfile dayProfile = (DayProfile)i.next();
            
            Structure dayProfileStruct = new Structure( );
            array.addDataType(dayProfileStruct);
            
            dayProfileStruct.addDataType( new Unsigned8( dayProfile.getDayId() ) );
            
            Array actionArray = new Array();
            dayProfileStruct.addDataType(actionArray);
            
            Iterator si = dayProfile.getSegments().iterator();
            while( si.hasNext() ) {
                
                DayProfileSegment segment = (DayProfileSegment) si.next();
                ActionItem action = segment.getAction();
                
                OctetString osStartTime = new OctetString( segment.getStartTimeOctets() );
                OctetString osScriptLn  = new OctetString( action.getLogicalNameOctets() );
                Unsigned16  osScript    = new Unsigned16( action.getSelector() );
                
                Structure segmentStruct = new Structure();
                segmentStruct.addDataType(osStartTime);
                segmentStruct.addDataType(osScriptLn);
                segmentStruct.addDataType(osScript);

                actionArray.addDataType(segmentStruct);
                
            }
            
        }
        
        return array; 
        
    }
    
}
