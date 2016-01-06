package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link NumericalRegister} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:50)
 */
public class NumericalRegisterImpl extends RegisterImpl<NumericalReading, NumericalRegisterSpec> implements NumericalRegister {

    public NumericalRegisterImpl(DeviceImpl device, NumericalRegisterSpec numericalRegisterSpec) {
        super(device, numericalRegisterSpec);
    }

    @Override
    protected NumericalReading newUnvalidatedReading(ReadingRecord actualReading) {
        return new NumericalReadingImpl(actualReading);
    }

    @Override
    protected NumericalReading newValidatedReading(ReadingRecord actualReading, DataValidationStatus validationStatus) {
        return new NumericalReadingImpl(actualReading, validationStatus);
    }

    @Override
    public Optional<ReadingType> getCalculatedReadingType(Instant timeStamp) {
        return getMultiplier(timeStamp).isPresent()? getRegisterSpec().getCalculatedReadingType() : Optional.empty();
    }

    private Optional<BigDecimal> getMultiplier(Instant timeStamp) {
        if (getRegisterSpec().isUseMultiplier()) {
            Optional<BigDecimal> multiplierAt = getDevice().getMultiplierAt(timeStamp);
            if(multiplierAt.isPresent() && multiplierAt.get().compareTo(BigDecimal.ONE) == 1){
                return multiplierAt;
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getMultiplier() {
        if (getRegisterSpec().isUseMultiplier()) {
            BigDecimal multiplier = getDevice().getMultiplier();
            if(multiplier.compareTo(BigDecimal.ONE) == 1){
                return Optional.of(multiplier);
            }
        }
        return Optional.empty();
    }
}