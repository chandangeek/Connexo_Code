package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned8;

public class Unsigned8Encoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
        
        return new Unsigned8( Integer.parseInt(""+value) ) ;
        
    }
    
}
