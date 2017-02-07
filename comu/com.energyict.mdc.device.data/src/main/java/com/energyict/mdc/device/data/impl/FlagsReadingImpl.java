/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.FlagsReading;

/**
 * Provides an implementation for the {@link FlagsReading} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (15:39)
 */
public class FlagsReadingImpl extends ReadingImpl implements FlagsReading {

    protected FlagsReadingImpl(ReadingRecord actualReading) {
        super(actualReading);
    }

    protected FlagsReadingImpl(ReadingRecord actualReading, DataValidationStatus validationStatus) {
        super(actualReading, validationStatus);
    }

    @Override
    public long getFlags() {
        return this.getActualReading().getValue().longValue();
    }

    @Override
    public boolean getFlagValue(int flagIndex) {
        return (this.getFlags() & (1L << flagIndex)) != 0;
    }

}