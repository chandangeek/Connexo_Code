package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import java.util.Calendar;
import java.util.TimeZone;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.DateTime;

public class DateEncoder implements Encoder {
    
    private TimeZone timeZone;
    
    public DateEncoder( TimeZone timeZone ) {
        this.timeZone = timeZone;
    }
    
    public AbstractDataType encode(Object value) {
        
        DateTime dateTime = new DateTime( );
        dateTime.setValue( Calendar.getInstance(timeZone) );
        
        return dateTime; 
        
    }
    
}
