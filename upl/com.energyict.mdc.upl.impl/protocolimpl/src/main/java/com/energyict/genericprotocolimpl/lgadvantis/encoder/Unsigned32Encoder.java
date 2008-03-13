package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Unsigned32;

public class Unsigned32Encoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
        
        return new Unsigned32( Integer.parseInt( "" +value) );
        
    }
     
}
