/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.fileimport.csvimport.FileImportRecord;

import java.time.Instant;
import java.util.Optional;

public class CustomPropertySetRecord extends FileImportRecord {
    private Instant versionId;
    private Instant startTime;
    private Instant endTime;
    private CustomPropertySetValues customPropertySetValues;

    public Optional<Instant> getVersionId() {
        return Optional.ofNullable(versionId);
    }

    public void setVersionId(Instant versionId) {
        this.versionId = versionId;
    }

    public Optional<Instant> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Optional<Instant> getEndTime() {
        return Optional.ofNullable(endTime);
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
        return versionId == null &&
                startTime == null &&
                endTime == null &&
                customPropertySetValues.isEmpty();
    }
}
