/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.BillingRegister;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link BillingRegister} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:50)
 */
public class BillingRegisterImpl extends RegisterImpl<BillingReading, NumericalRegisterSpec> implements BillingRegister {

    public BillingRegisterImpl(DeviceImpl device, NumericalRegisterSpec registerSpec) {
        super(device, registerSpec);
    }

    @Override
    protected BillingReading newUnvalidatedReading(ReadingRecord actualReading) {
        return new BillingReadingImpl(actualReading);
    }

    @Override
    protected BillingReading newValidatedReading(ReadingRecord actualReading, DataValidationStatus validationStatus) {
        return new BillingReadingImpl(actualReading, validationStatus);
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