package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.NumericalReading;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;

import com.energyict.mdc.device.data.NumericalRegister;
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

    protected NumericalReadingImpl(ReadingRecord actualReading, NumericalRegister register, ReadingRecord previousReading) {
        super(actualReading, register, previousReading);
    }

    protected NumericalReadingImpl(ReadingRecord actualReading, DataValidationStatus validationStatus, NumericalRegister register, ReadingRecord previousReading) {
        super(actualReading, validationStatus, register, previousReading);
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
    public Optional<Quantity> getCollectedValue() {
        return Optional.ofNullable(getQuantityFor(getRegister().getReadingType()));
    }

    @Override
    public Optional<Quantity> getCalculatedValue() {
        return Optional.ofNullable(getCalculatedReadingType(getTimeStamp()).map(this::getQuantityFor).orElse(null));
    }

    private Optional<ReadingType> getCalculatedReadingType(Instant timeStamp) {
        return getRegister().getCalculatedReadingType(timeStamp);
    }

    @Override
    public Optional<BigDecimal> getDelta() {
        //todo verify whether the getCalculatedReadingType call isn't blocking performance
        if (getPreviousReading().isPresent()) {
            ReadingRecord previousReadingRecord = getPreviousReading().get();
            if (this.getCalculatedValue().isPresent()) {
                Optional<ReadingType> calculatedReadingTypeForPrevious = getCalculatedReadingType(previousReadingRecord.getTimeStamp());
                if (calculatedReadingTypeForPrevious.isPresent()) {
                    Quantity previousQuantity = previousReadingRecord.getQuantity(calculatedReadingTypeForPrevious.get());
                    if (previousQuantity != null) {
                        return Optional.of(getCalculatedValue().get().getValue().subtract(previousQuantity.getValue()));
                    }
                }
            } else {
                Quantity previousQuantity = previousReadingRecord.getQuantity(previousReadingRecord.getReadingType());
                if (previousQuantity != null) {
                    return Optional.of(getCollectedValue().get().getValue().subtract(previousQuantity.getValue()));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Range<Instant>> getRange() {
        if (getRegister().getReadingType().isCumulative()) {
            if (getPreviousReading().isPresent()) {
                return Optional.of(Range.openClosed(getPreviousReading().get().getTimeStamp(), getActualReading().getTimeStamp()));
            } else {
                return Optional.of(Range.atMost(this.getActualReading().getTimeStamp()));
            }
        } else if (getRegister().getReadingType().getMacroPeriod().equals(MacroPeriod.BILLINGPERIOD)) {
            return this.getActualReading().getTimePeriod();
        } else {
            return Optional.empty();
        }
    }

}