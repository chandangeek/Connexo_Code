package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.genericprotocolimpl.lgadvantis.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

class ActivityCalendarParser extends AbstractParser implements Parser {
    
    private TimeZone timeZone;
    
    public ActivityCalendarParser( TimeZone timeZone ) {
        this.timeZone = timeZone;
    }
    
    ActivityCalendarParser( ) { }
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        ObisCode obis = getAttribute().getObisCode();
        
        OctetString os = (OctetString) dataType;
        byte ber [] = os.getBEREncodedByteArray();
        DateTime dt = new DateTime(ber, 0, timeZone );
        
        Calendar c = dt.getValue();
        BigDecimal amount = new BigDecimal( c.getTimeInMillis() );
        Quantity quantity = new Quantity( amount, Unit.getUndefined() );
    
        task.addRegisterValue(new RegisterValue( obis, quantity ) ); 
        
    }
    
}
