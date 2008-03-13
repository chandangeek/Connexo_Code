package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.genericprotocolimpl.lgadvantis.Task;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

public class MeterIdentificationParser extends AbstractParser implements Parser {
    
    
    public MeterIdentificationParser( ) {}
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        ObisCode obis = getAttribute().getObisCode();

        String value = "";
        
        if( dataType == null ) {
            return;
        } else if( dataType instanceof VisibleString ) { // :-S
            value = ( (VisibleString)dataType ).getStr();
        } else {
            value = dataType.toBigDecimal().toString();
        }
        
        task.addRegisterValue( new RegisterValue( obis, value ) );
  
    }
    
}
