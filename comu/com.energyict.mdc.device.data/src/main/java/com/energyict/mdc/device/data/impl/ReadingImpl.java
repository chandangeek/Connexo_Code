/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.Reading;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides code reuse opportunities for components
 * that implement of the sub-interfaces of {@link Reading}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (14:56)
 */
public abstract class ReadingImpl implements Reading {

    private final ReadingRecord actualReading;
    private final Optional<DataValidationStatus> validationStatus;

    /**
     * Creates a new Reading that is marked as not validated.
     *
     * @param actualReading The actual ReadingRecord from the Jupiter Kore bundle
     */
    protected ReadingImpl(ReadingRecord actualReading) {
        super();
        this.actualReading = actualReading;
        this.validationStatus = Optional.empty();
    }

    /**
     * Creates a new Reading that is marked as validated.
     *
     * @param actualReading The actual ReadingRecord from the Jupiter Kore bundle
     * @param validationStatus The List of ReadingQuality
     */
    protected ReadingImpl(ReadingRecord actualReading, DataValidationStatus validationStatus) {
        super();
        this.actualReading = actualReading;
        this.validationStatus = Optional.of(validationStatus);
    }

    @Override
    public ReadingType getType () {
        return this.actualReading.getReadingType();
    }

    @Override
    public ReadingRecord getActualReading() {
        return this.actualReading;
    }

    @Override
    public Instant getTimeStamp() {
        return this.actualReading.getTimeStamp();
    }

    @Override
    public Instant getReportedDateTime() {
        return this.actualReading.getReportedDateTime();
    }

    @Override
    public BigDecimal getSensorAccuracy() {
        return this.actualReading.getSensorAccuracy();
    }

    @Override
    public String getSource() {
        return this.actualReading.getSource();
    }

    @Override
    public Optional<DataValidationStatus> getValidationStatus() {
        return this.validationStatus;
    }

}