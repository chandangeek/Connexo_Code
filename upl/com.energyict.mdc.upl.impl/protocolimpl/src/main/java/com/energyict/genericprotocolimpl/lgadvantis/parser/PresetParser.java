package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.obis.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.edf.messages.objects.DemandManagement;
import com.energyict.genericprotocolimpl.lgadvantis.*;
import com.energyict.protocol.RegisterValue;

public class PresetParser extends AbstractParser implements Parser {
    
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {
        
        ObisCode obis = getAttribute().getObisCode();
        
        Array array = (Array) dataType;
        
        int max = array.getDataType(0).intValue();
        int subscribed = array.getDataType(1).intValue();

        String xml = new DemandManagement( max, subscribed ).xmlEncode();
        
        
        task.addRegisterValue( new RegisterValue(obis, xml) );
  
    }
    
}
