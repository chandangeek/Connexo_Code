package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cps.CustomPropertySetValues;

import java.time.Instant;

/**
 * Created by antfom on 02.03.2016.
 */
public class CustomPropertySetRecord {
    private Instant versionId;
    private Instant startTime;
    private Instant endTime;
    private CustomPropertySetValues customPropertySetValues;

    public Instant getVersionId() {
        return versionId;
    }

    public void setVersionId(Instant versionId) {
        this.versionId = versionId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public CustomPropertySetValues getCustomPropertySetValues() {
        return customPropertySetValues;
    }

    public void setCustomPropertySetValues(CustomPropertySetValues customPropertySetValues) {
        this.customPropertySetValues = customPropertySetValues;
    }

    public boolean isEmpty() {
        return versionId != null &&
                startTime != null &&
                endTime != null &&
                !customPropertySetValues.isEmpty();
    }
}
