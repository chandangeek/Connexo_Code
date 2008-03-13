package com.energyict.genericprotocolimpl.lgadvantis.collector;

import com.energyict.genericprotocolimpl.lgadvantis.*;

public interface Collector {
    
    public ReadResult getAll( Task task, RtuMessageLink messageLink );
    
}
