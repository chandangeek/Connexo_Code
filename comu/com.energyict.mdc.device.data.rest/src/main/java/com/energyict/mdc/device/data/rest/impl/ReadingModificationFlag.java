package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;

public enum ReadingModificationFlag {
    
    ADDED,
    EDITED,
    REMOVED;
    
    public static ReadingModificationFlag getFlag(BaseReadingRecord readingRecord) {
        if (readingRecord.wasAdded()) {
            return ADDED;
        }
        if (readingRecord.edited()) {
            return EDITED;
        }
        return null;
    }
}
