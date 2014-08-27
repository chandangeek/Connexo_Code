package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Reading;

import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Provides code reuse opportunities for components
 * that implement of the sub-interfaces of {@link Reading}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (14:56)
 */
public abstract class ReadingImpl implements Reading {

    private final ReadingRecord actualReading;
    private final boolean validated;
    private final List<ReadingQuality> readingQualities;

    /**
     * Creates a new Reading that is marked as not validated.
     *
     * @param actualReading The actual ReadingRecord from the Jupiter Kore bundle
     */
    protected ReadingImpl(ReadingRecord actualReading) {
        super();
        this.actualReading = actualReading;
        this.validated = false;
        this.readingQualities = new ArrayList<>();
    }

    /**
     * Creates a new Reading that is marked as validated.
     *
     * @param actualReading The actual ReadingRecord from the Jupiter Kore bundle
     * @param readingQualities The List of ReadingQuality
     */
    protected ReadingImpl(ReadingRecord actualReading, List<ReadingQuality> readingQualities) {
        super();
        this.actualReading = actualReading;
        this.validated = true;
        this.readingQualities = readingQualities;
    }

    @Override
    public ReadingType getType () {
        return this.actualReading.getReadingType();
    }

    protected ReadingRecord getActualReading() {
        return this.actualReading;
    }

    @Override
    public Date getTimeStamp() {
        return this.actualReading.getTimeStamp();
    }

    @Override
    public Date getReportedDateTime() {
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
    public boolean isValidated() {
        return this.validated;
    }

    @Override
    public List<ReadingQuality> getReadingQualities() {
        return Collections.unmodifiableList(this.readingQualities);
    }

}