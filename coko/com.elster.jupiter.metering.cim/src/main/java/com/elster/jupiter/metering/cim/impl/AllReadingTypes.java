/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim.impl;

import com.elster.jupiter.metering.ReadingType;

public enum AllReadingTypes implements ReadingTypeFilter {
    INSTANCE;

    @Override
    public boolean apply(ReadingType readingType) {
        return true;
    }
}
