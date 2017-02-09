package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Reading;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.Register;

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
    private final Optional<ReadingRecord> previousReading;
    private final Optional<DataValidationStatus> validationStatus;
    private final Register<?, ?> register;

    /**
     * Creates a new Reading that is marked as not validated.
     *  @param actualReading The actual ReadingRecord from the Jupiter Kore bundle
     * @param register the register for which this reading is applicable
     * @param previousReading The previous ReadingRecord from the Jupiter Kore bundle
     */
    protected ReadingImpl(ReadingRecord actualReading, Register<?, ?> register, ReadingRecord previousReading) {
        super();
        this.actualReading = actualReading;
        this.register = register;
        this.validationStatus = Optional.empty();
        this.previousReading = Optional.ofNullable(previousReading);
    }

    /**
     * Creates a new Reading that is marked as validated.
     * @param actualReading The actual ReadingRecord from the Jupiter Kore bundle
     * @param validationStatus The List of ReadingQuality
     * @param register the register for which this reading is applicable
     * @param previousReading The previous ReadingRecord from the Jupiter Kore bundle
     */
    protected ReadingImpl(ReadingRecord actualReading, DataValidationStatus validationStatus, Register<?, ?> register, ReadingRecord previousReading) {
        super();
        this.actualReading = actualReading;
        this.register = register;
        this.validationStatus = Optional.of(validationStatus);
        this.previousReading = Optional.ofNullable(previousReading);
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

    @Override
    public Optional<Instant> getEventDate() {
        return getRegister().hasEventDate()? Optional.of(getTimeStamp()):Optional.empty();
    }

    Register<?, ?> getRegister() {
        return register;
    }

    Optional<ReadingRecord> getPreviousReading() {
        return previousReading;
    }
}