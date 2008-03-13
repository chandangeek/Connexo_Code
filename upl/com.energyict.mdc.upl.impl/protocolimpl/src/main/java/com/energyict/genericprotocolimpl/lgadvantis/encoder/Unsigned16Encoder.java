package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import com.energyict.dlms.axrdencoding.*;

public class Unsigned16Encoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
        
        return new Unsigned16( Integer.parseInt( "" +value) );
        
    }
     
}
