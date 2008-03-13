package com.energyict.genericprotocolimpl.lgadvantis.collector;

import com.energyict.genericprotocolimpl.lgadvantis.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

public class ActivityCalendarCollector implements Collector {
    
    CosemFactory cosemFactory;
    
    public ActivityCalendarCollector( CosemFactory cosemFactory ) {
        this.cosemFactory = cosemFactory;
    }
    
    public ReadResult getAll(Task task, RtuMessageLink messageLink) {
       
        ReadResult result = new ReadResult( );
        
        ObisCode oc = cosemFactory.getActivityCalendar().getObisCode();
        String txt = task.getActivityCalendar().xmlEncode();
        
        result.addRegisterValue( new RegisterValue(oc, txt) );
        
        return result;
    
    }
    
}
