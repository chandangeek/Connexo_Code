package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;

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
    protected NumericalReading newUnvalidatedReading(ReadingRecord actualReading, ReadingRecord previousReading) {
        return new NumericalReadingImpl(actualReading, this, previousReading);
    }

    @Override
    protected NumericalReading newValidatedReading(ReadingRecord actualReading, DataValidationStatus validationStatus, ReadingRecord previousReading) {
        return new NumericalReadingImpl(actualReading, validationStatus, this, previousReading);
    }

    @Override
    public Optional<ReadingType> getCalculatedReadingType(Instant timeStamp) {
        return device.getCalculatedReadingTypeFromMeterConfiguration(getRegisterSpec().getReadingType(), timeStamp);
    }

    @Override
    public Optional<BigDecimal> getMultiplier(Instant timeStamp) {
        return getRegisterSpec().isUseMultiplier()?getDevice().getMultiplierAt(timeStamp):Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getOverflow() {
        Optional<MeterReadingTypeConfiguration> registerReadingTypeConfiguration = this.device.getMeterReadingTypeConfigurationFor(this.getReadingType());
        if (registerReadingTypeConfiguration.isPresent()) {
            Optional<BigDecimal> overflowValue = registerReadingTypeConfiguration.get().getOverflowValue();
            if (overflowValue.isPresent()) {
                return overflowValue;
            } else {
                return getRegisterSpec().getOverflowValue();
            }
        } else {
            return getRegisterSpec().getOverflowValue();
        }
    }

    @Override
    public int getNumberOfFractionDigits() {
        Optional<MeterReadingTypeConfiguration> registerReadingTypeConfiguration = this.device.getMeterReadingTypeConfigurationFor(this.getReadingType());
        if (registerReadingTypeConfiguration.isPresent()) {
            return registerReadingTypeConfiguration.get().getNumberOfFractionDigits().orElse(getRegisterSpec().getNumberOfFractionDigits());
        } else {
            return getRegisterSpec().getNumberOfFractionDigits();
        }
    }
}