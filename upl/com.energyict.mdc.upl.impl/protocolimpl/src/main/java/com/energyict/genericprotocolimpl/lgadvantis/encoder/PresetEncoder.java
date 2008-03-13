package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.edf.messages.objects.DemandManagement;

public class PresetEncoder implements Encoder {
    
    public AbstractDataType encode(Object value) {
        
        DemandManagement dm = (DemandManagement) value;
        
        Array array = new Array( );
        
        array.addDataType( new Unsigned16( dm.getMaxloadThreshold() ) );
        array.addDataType( new Unsigned16( dm.getSubscribedThreshold() ) );
        
        return array;
         
    }
    
}
