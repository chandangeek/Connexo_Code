package com.energyict.genericprotocolimpl.lgadvantis.parser;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.genericprotocolimpl.lgadvantis.Task;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edf.messages.objects.MeterClock;

import java.io.IOException;
import java.util.TimeZone;

public class DateTimeParser extends AbstractParser implements Parser {
    
    private TimeZone timeZone;
    
    public DateTimeParser( TimeZone timeZone ) {
        this.timeZone = timeZone;
    }
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        ObisCode obis = getAttribute().getObisCode();
        
        OctetString os = (OctetString) dataType;
        byte ber [] = os.getBEREncodedByteArray();
        DateTime dt = new DateTime(ber, 0, timeZone );
        
        System.out.println( dt.getValue().getTime() );
        
        MeterClock mc = new MeterClock( dt.getValue(), dt.getStatus() > 128 );
        String xml = mc.xmlEncode();
        
        task.addRegisterValue( new RegisterValue( obis, xml ) );
        
    }
    
}
