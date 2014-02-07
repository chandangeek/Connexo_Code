package com.elster.jupiter.metering.cim.impl;

import com.elster.jupiter.metering.ReadingType;

public enum AllReadingTypes implements ReadingTypeFilter {
    INSTANCE;

    @Override
    public boolean apply(ReadingType readingType) {
        return true;
    }
}
