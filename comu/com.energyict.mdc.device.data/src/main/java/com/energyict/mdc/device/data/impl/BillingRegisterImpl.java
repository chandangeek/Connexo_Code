package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.BillingRegister;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;

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
        Optional<BigDecimal> multiplierAt = getDevice().getMultiplierAt(timeStamp);
        if (multiplierAt.isPresent() && multiplierAt.get().compareTo(BigDecimal.ONE) == 1) {
            return device.getCalculatedReadingTypeFromMeterConfiguration(getRegisterSpec().getReadingType(), timeStamp);
        }
        return Optional.empty();
    }

    @Override
    public Optional<BigDecimal> getMultiplier(Instant timeStamp) {
        Optional<BigDecimal> multiplierAt = getDevice().getMultiplierAt(timeStamp);
        if (multiplierAt.isPresent() && multiplierAt.get().compareTo(BigDecimal.ONE) == 1) {
            Optional<ReadingType> koreMeterConfigBulkReadingType = device.getCalculatedReadingTypeFromMeterConfiguration(getRegisterSpec().getReadingType(), timeStamp);
            if (koreMeterConfigBulkReadingType.isPresent()) { // if it is present, then it means we configured a ReadingType to calculate
                return multiplierAt;
            }
        }
        return Optional.empty();
    }

}