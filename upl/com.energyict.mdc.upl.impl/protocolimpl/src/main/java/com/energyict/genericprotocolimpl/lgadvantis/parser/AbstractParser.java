package com.energyict.genericprotocolimpl.lgadvantis.parser;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.*;

abstract class AbstractParser implements Parser { 

    private CosemAttribute attribute;
    
    public abstract void parse(AbstractDataType dataType, Task task) 
        throws IOException;
    
    public void setAttribute(CosemAttribute attribute) {
        this.attribute = attribute;
    }
    
    public CosemAttribute getAttribute( ) {
        return attribute;
    }
     
}
