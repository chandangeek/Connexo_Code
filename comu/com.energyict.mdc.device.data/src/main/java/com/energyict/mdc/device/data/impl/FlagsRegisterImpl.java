package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.FlagsReading;
import com.energyict.mdc.device.data.FlagsRegister;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;

/**
 * Provides an implementation for the {@link FlagsRegister} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:50)
 */
public class FlagsRegisterImpl extends RegisterImpl<FlagsReading> implements FlagsRegister {

    public FlagsRegisterImpl(DeviceImpl device, RegisterSpec registerSpec) {
        super(device, registerSpec);
    }

    @Override
    protected FlagsReading newUnvalidatedReading(ReadingRecord actualReading) {
        return new FlagsReadingImpl(actualReading);
    }

    @Override
    protected FlagsReading newValidatedReading(ReadingRecord actualReading, DataValidationStatus validationStatus) {
        return new FlagsReadingImpl(actualReading, validationStatus);
    }

}