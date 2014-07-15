package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.NumericalReading;

import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Provides an implementation for the {@link NumericalReading} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (14:57)
 */
public class NumericalReadingImpl extends ReadingImpl implements NumericalReading {

    protected NumericalReadingImpl(ReadingRecord actualReading) {
        super(actualReading);
    }

    protected NumericalReadingImpl(ReadingRecord actualReading, List<ReadingQuality> readingQualities) {
        super(actualReading, readingQualities);
    }

    @Override
    public Quantity getQuantity() {
        return this.getActualReading().getQuantity(0);
    }

    @Override
    public BigDecimal getValue() {
        return this.getActualReading().getValue();
    }

}