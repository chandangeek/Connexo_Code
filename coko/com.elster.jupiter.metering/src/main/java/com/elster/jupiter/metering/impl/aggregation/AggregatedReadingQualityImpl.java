/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.aggregation.AggregatedReadingQuality;

import java.time.Instant;
import java.util.Optional;

class AggregatedReadingQualityImpl implements AggregatedReadingQuality {

    private final ReadingType readingType;
    private final ReadingQualityType readingQualityType;
    private final Instant readingTimestamp;

    AggregatedReadingQualityImpl(ReadingType readingType, ReadingQualityType readingQualityType, Instant readingTimestamp) {
        this.readingType = readingType;
        this.readingQualityType = readingQualityType;
        this.readingTimestamp = readingTimestamp;
    }

    @Override
    public Instant getTimestamp() {
        return readingTimestamp;
    }

    @Override
    public Channel getChannel() {
        return null;
    }

    @Override
    public CimChannel getCimChannel() {
        return null;
    }

    @Override
    public ReadingType getReadingType() {
        return readingType;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setComment(String comment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<BaseReadingRecord> getBaseReadingRecord() {
        return Optional.empty();
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Instant getReadingTimestamp() {
        return readingTimestamp;
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getVersion() {
        return 0;
    }

    @Override
    public boolean isActual() {
        return true;
    }

    @Override
    public void makePast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void makeActual() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getComment() {
        return "";
    }

    @Override
    public String getTypeCode() {
        return getType().getCode();
    }

    @Override
    public ReadingQualityType getType() {
        return readingQualityType;
    }

    public boolean isSuspect() {
        return getType().getCode().equals(ReadingQuality.DERIVED_SUSPECT.getCode());
    }

    public boolean isMissing() {
        return getType().getCode().equals(ReadingQuality.DERIVED_MISSING.getCode());
    }

    public boolean isIndeterministic() {
        return getType().getCode().equals(ReadingQuality.DERIVED_INDETERMINISTIC.getCode());
    }

    public boolean hasEditCategory() {
        throw new UnsupportedOperationException();
    }

    public boolean hasEstimatedCategory() {
        throw new UnsupportedOperationException();
    }
}
