/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;

public class AdvanceReadingsSettingsInfo {

    public boolean none = false;
    public boolean bulk = false;
    public ReadingTypeInfo readingType;

    public AdvanceReadingsSettingsInfo() {}

    public AdvanceReadingsSettingsInfo(AdvanceReadingsSettings advanceReadingsSettings,
                                       ReadingTypeInfoFactory readingTypeInfoFactory) {
        if (advanceReadingsSettings instanceof NoneAdvanceReadingsSettings) {
            none = true;
        }
        if (advanceReadingsSettings instanceof BulkAdvanceReadingsSettings) {
            bulk = true;
        }
        if (advanceReadingsSettings instanceof ReadingTypeAdvanceReadingsSettings) {
            readingType = readingTypeInfoFactory.from(((ReadingTypeAdvanceReadingsSettings) advanceReadingsSettings).getReadingType());
        }
    }


    public String toString() {
        if (readingType != null) {
            return readingType.mRID;
        } else if (bulk) {
            return BulkAdvanceReadingsSettings.BULK_ADVANCE_READINGS_SETTINGS;
        } else {
            return NoneAdvanceReadingsSettings.NONE_ADVANCE_READINGS_SETTINGS;
        }
    }

}

