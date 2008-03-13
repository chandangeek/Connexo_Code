package com.energyict.genericprotocolimpl.lgadvantis.collector;

import java.util.*;

import com.energyict.genericprotocolimpl.lgadvantis.*;
import com.energyict.obis.ObisCode;

public class DefaultCosemObjectCollector implements Collector {
    
    public ReadResult getAll(Task task, RtuMessageLink messageLink) {
        
        ReadResult rr = new ReadResult();
        
        Iterator i = messageLink.getDirectactions().iterator();
        
        while ( i.hasNext() ) {
            
            DirectAction directAction = (DirectAction) i.next();
            ObisCode obisCode = directAction.getCosem().getObisCode();
            
            if( directAction.isRead() ) {
                rr.addAllRegisterValues( task.findRegisterValues( obisCode, true ) );
            }
        }
        
        return rr;
    }
    
}
