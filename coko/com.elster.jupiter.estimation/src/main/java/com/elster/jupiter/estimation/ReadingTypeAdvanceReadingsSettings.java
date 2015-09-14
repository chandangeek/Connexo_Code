package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;

/**
 * Created by igh on 2/04/2015.
 */
public class ReadingTypeAdvanceReadingsSettings implements AdvanceReadingsSettings {

    private ReadingType readingType;

    public ReadingTypeAdvanceReadingsSettings(ReadingType readingType) {
        this.readingType = readingType;
    }

    public String toString() {
        return readingType != null ? readingType.getMRID() : "";
    }

    public ReadingType getReadingType() {
        return this.readingType;
    }
}
