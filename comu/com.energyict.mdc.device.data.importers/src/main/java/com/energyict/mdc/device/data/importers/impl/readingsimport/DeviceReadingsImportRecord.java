/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeviceReadingsImportRecord extends FileImportRecord {

    private ZonedDateTime readingDateTime;
    private List<String> readingTypes = new ArrayList<>();
    private List<BigDecimal> values = new ArrayList<>();

    public void setReadingDateTime(ZonedDateTime readingDateTime) {
        this.readingDateTime = readingDateTime;
    }

    public void addReadingType(String readingType) {
        readingTypes.add(readingType);
    }

    public void addReadingValue(BigDecimal value) {
        values.add(value);
    }

    public ZonedDateTime getReadingDateTime() {
        return readingDateTime;
    }

    public List<BigDecimal> getValues() {
        return values;
    }

    public List<String> getReadingTypes() {
        return readingTypes;
    }
}
