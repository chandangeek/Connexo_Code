package com.elster.jupiter.validation.rest;

import com.elster.jupiter.estimation.AdvanceReadingsSettings;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;

/**
 * Created by igh on 3/04/2015.
 */
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

}
