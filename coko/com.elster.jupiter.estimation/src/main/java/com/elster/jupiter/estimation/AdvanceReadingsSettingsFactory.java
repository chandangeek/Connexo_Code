/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.AbstractValueFactory;

public class AdvanceReadingsSettingsFactory extends AbstractValueFactory<AdvanceReadingsSettings> {

    private MeteringService meteringService;

    public AdvanceReadingsSettingsFactory(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public Class<AdvanceReadingsSettings> getValueType () {
        return AdvanceReadingsSettings.class;
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public AdvanceReadingsSettings valueFromDatabase (Object object) {
        return fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase (AdvanceReadingsSettings advanceReadingsSettings) {
        return toStringValue(advanceReadingsSettings);
    }

    @Override
    public AdvanceReadingsSettings fromStringValue (String stringValue) {
        if (NoneAdvanceReadingsSettings.NONE_ADVANCE_READINGS_SETTINGS.equals(stringValue)) {
            return NoneAdvanceReadingsSettings.INSTANCE;
        } else if (BulkAdvanceReadingsSettings.BULK_ADVANCE_READINGS_SETTINGS.equals(stringValue)) {
            return BulkAdvanceReadingsSettings.INSTANCE;
        } else {
            ReadingType readingType = meteringService.getReadingType(stringValue).orElse(null);
            return new ReadingTypeAdvanceReadingsSettings(readingType);
        }
    }

    @Override
    public String toStringValue (AdvanceReadingsSettings advanceReadingsSettings) {
        return advanceReadingsSettings.toString();
    }

}
