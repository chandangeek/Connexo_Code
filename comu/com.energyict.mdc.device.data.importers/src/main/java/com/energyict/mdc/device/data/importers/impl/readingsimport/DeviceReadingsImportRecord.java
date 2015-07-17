package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DeviceReadingsImportRecord extends FileImportRecord {

    private ZonedDateTime readingDateTime;

    private List<Pair<String, BigDecimal>> readingsPerChannel = new ArrayList<>();

    public DeviceReadingsImportRecord(long lineNumber) {
        super(lineNumber);
    }

    public void setReadingDateTime(ZonedDateTime readingDateTime) {
        this.readingDateTime = readingDateTime;
    }

    public void addReading(String readingType, BigDecimal value) {
        readingsPerChannel.add(Pair.of(readingType, value));
    }
}
