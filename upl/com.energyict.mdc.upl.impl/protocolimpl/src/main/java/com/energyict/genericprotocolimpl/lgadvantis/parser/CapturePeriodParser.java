package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.Task;

public class CapturePeriodParser extends AbstractParser implements Parser {
    
    
    public void parse(AbstractDataType dataType, Task task) 
        throws IOException {

        task.setInterval( dataType.intValue() );
  
    }
    
}
