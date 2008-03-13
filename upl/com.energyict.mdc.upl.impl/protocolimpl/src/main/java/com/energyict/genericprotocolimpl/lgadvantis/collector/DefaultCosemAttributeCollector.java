package com.energyict.genericprotocolimpl.lgadvantis.collector;

import java.util.*;

import com.energyict.genericprotocolimpl.lgadvantis.*;
import com.energyict.obis.ObisCode;

public class DefaultCosemAttributeCollector implements Collector {
    
    public ReadResult getAll(Task task, RtuMessageLink messageLink) {
        
        ReadResult r = new ReadResult();
        
        Iterator i = messageLink.getDirectactions().iterator();
        
        while ( i.hasNext() ) {
            
            DirectAction directAction = (DirectAction) i.next();
            ObisCode obisCode = directAction.getCosem().getObisCode();
            
            r.addAllRegisterValues( task.findRegisterValues( obisCode, true ) ); 
            
        }
        
        return r;
    }
    
}
