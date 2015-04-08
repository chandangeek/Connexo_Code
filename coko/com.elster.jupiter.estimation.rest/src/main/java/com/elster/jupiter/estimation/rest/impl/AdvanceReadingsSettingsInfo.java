package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;

public class AdvanceReadingsSettingsInfo {

    public boolean none = false;
    public boolean bulk = false;
    public ReadingTypeInfo readingType;

    public AdvanceReadingsSettingsInfo() {}

    public AdvanceReadingsSettingsInfo(AdvanceReadingsSettings advanceReadingsSettings) {
        if (advanceReadingsSettings instanceof NoneAdvanceReadingsSettings) {
            none = true;
        }
        if (advanceReadingsSettings instanceof BulkAdvanceReadingsSettings) {
            bulk = true;
        }
        if (advanceReadingsSettings instanceof ReadingTypeAdvanceReadingsSettings) {
            readingType = new ReadingTypeInfo(((ReadingTypeAdvanceReadingsSettings) advanceReadingsSettings).getReadingType());
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

