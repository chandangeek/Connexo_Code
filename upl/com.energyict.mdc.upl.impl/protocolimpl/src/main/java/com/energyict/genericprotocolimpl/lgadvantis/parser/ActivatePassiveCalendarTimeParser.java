package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.edf.messages.objects.CosemCalendar;
import com.energyict.edf.messages.objects.OctetString;
import com.energyict.genericprotocolimpl.lgadvantis.*;

public class ActivatePassiveCalendarTimeParser extends AbstractParser implements Parser {
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        byte ber [] = dataType.getOctetString().getBEREncodedByteArray();
        
        OctetString oc = new OctetString( ber );
        CosemCalendar cc = new CosemCalendar( oc );
        
        task.getActivityCalendar().setActivatePassiveCalendarTime(cc); 
        
    }
    
}
 