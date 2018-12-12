/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.FlagsReading;
import com.energyict.mdc.device.data.FlagsRegister;

/**
 * Provides an implementation for the {@link FlagsRegister} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:50)
 */
public class FlagsRegisterImpl extends RegisterImpl<FlagsReading, NumericalRegisterSpec> implements FlagsRegister {

    public FlagsRegisterImpl(DeviceImpl device, NumericalRegisterSpec registerSpec) {
        super(device, registerSpec);
    }

    @Override
    protected FlagsReading newUnvalidatedReading(ReadingRecord actualReading, ReadingRecord previousReading) {
        return new FlagsReadingImpl(actualReading, this, previousReading);
    }

    @Override
    protected FlagsReading newValidatedReading(ReadingRecord actualReading, DataValidationStatus validationStatus, ReadingRecord previous) {
        return new FlagsReadingImpl(actualReading, validationStatus, this, previous);
    }

}