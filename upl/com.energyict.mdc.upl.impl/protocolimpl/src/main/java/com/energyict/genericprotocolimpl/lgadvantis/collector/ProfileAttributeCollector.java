package com.energyict.genericprotocolimpl.lgadvantis.collector;

import com.energyict.genericprotocolimpl.lgadvantis.*;

public class ProfileAttributeCollector implements Collector {
    
    public ReadResult getAll(Task task, RtuMessageLink messageLink) {
        
        ReadResult r = new ReadResult();
        
        r.setProfileData( task.getProfileData() );
        
        return r;

    }
    
}
