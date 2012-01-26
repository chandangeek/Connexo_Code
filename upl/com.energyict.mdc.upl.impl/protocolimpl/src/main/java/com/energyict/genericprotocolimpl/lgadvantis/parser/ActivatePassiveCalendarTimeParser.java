package com.energyict.genericprotocolimpl.lgadvantis.parser;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.Task;
import com.energyict.protocolimpl.edf.messages.objects.CosemCalendar;
import com.energyict.protocolimpl.edf.messages.objects.OctetString;

import java.io.IOException;

public class ActivatePassiveCalendarTimeParser extends AbstractParser implements Parser {
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        byte ber [] = dataType.getOctetString().getBEREncodedByteArray();
        
        OctetString oc = new OctetString( ber );
        CosemCalendar cc = new CosemCalendar( oc );
        
        task.getActivityCalendar().setActivatePassiveCalendarTime(cc); 
        
    }
    
}
 