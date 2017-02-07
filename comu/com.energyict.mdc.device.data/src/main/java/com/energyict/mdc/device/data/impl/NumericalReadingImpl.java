package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.NumericalReading;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link NumericalReading} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (14:57)
 */
public class NumericalReadingImpl extends ReadingImpl implements NumericalReading {

    private BigDecimal delta;

    protected NumericalReadingImpl(ReadingRecord actualReading) {
        super(actualReading);
    }

    protected NumericalReadingImpl(ReadingRecord actualReading, DataValidationStatus validationStatus) {
        super(actualReading, validationStatus);
    }

    @Override
    public Quantity getQuantity() {
        return this.getActualReading().getQuantity(0);
    }

    @Override
    public Quantity getQuantityFor(ReadingType readingType) {
        return getActualReading().getQuantity(readingType);
    }

    @Override
    public BigDecimal getValue() {
        return this.getActualReading().getValue();
    }

    @Override
    public Optional<BigDecimal> getDelta() {
        return Optional.ofNullable(delta);
    }

    @Override
    public Optional<Range<Instant>> getRange() {
        return Optional.of(Range.atMost(this.getActualReading().getTimeStamp()));
    }

}