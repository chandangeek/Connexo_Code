/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class UsagePointImportRecordModel {

    private String usagePointName;
    private Instant readingDate;
    private String purpose;
    private long lineNumber;
    private Map<String, BigDecimal> mapBetweenReadingTypeAndValue;

    public String getUsagePointName() {
        return usagePointName;
    }

    public void setUsagePointName(String usagePointName) {
        this.usagePointName = usagePointName;
    }

    public Instant getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(Instant readingDate) {
        this.readingDate = readingDate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Map<String, BigDecimal> getMapBetweenReadingTypeAndValue() {
        return mapBetweenReadingTypeAndValue;
    }

    public void setMapBetweenReadingTypeAndValue(Map<String, BigDecimal> mapBetweenReadingTypeAndValue) {
        this.mapBetweenReadingTypeAndValue = mapBetweenReadingTypeAndValue;
    }

    public enum UsagePointImportRecordMapping {
        USAGE_POINT_NAME("Usage point name", "usagePointName"),
        READING_DATE("Reading date", "readingDate"),
        PURPOSE("Purpose", "purpose"),
        TYPE_AND_VALUE("", "mapBetweenReadingTypeAndValue"),
        RECORD_LINE_NUMBER("", "lineNumber");

        private final String csvHeader;
        private final String objectField;

        UsagePointImportRecordMapping(String csvHeader, String objectField) {
            this.csvHeader = csvHeader;
            this.objectField = objectField;
        }

        public String getCsvHeader() {
            return csvHeader;
        }

        public String getObjectField() {
            return objectField;
        }
    }
}
