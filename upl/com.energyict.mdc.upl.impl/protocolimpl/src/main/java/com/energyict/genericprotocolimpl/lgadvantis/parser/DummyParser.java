package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.cbo.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.genericprotocolimpl.lgadvantis.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;


public class DummyParser extends AbstractParser implements Parser {
    
    private Unit unit;
  
    public DummyParser( Unit unit ) {
        this.unit = unit;
    }
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        ObisCode obis = getAttribute().getObisCode();
        
        if( dataType == null ) {
            return;
        
        } else if( dataType instanceof VisibleString ) { // :-S
        
            String value = ( (VisibleString)dataType ).getStr();
            task.addRegisterValue( new RegisterValue( obis, value ) );
        
        } else {
        
            BigDecimal amount = dataType.toBigDecimal();
            Quantity q = new Quantity( amount, unit );
            task.addRegisterValue( new RegisterValue( obis, q ) );
        
        }
        
  
    }
    
}
