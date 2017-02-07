/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.BillingReading;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link BillingReading} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (15:39)
 */
public class BillingReadingImpl extends NumericalReadingImpl implements BillingReading {

    protected BillingReadingImpl(ReadingRecord actualReading) {
        super(actualReading);
    }

    protected BillingReadingImpl(ReadingRecord actualReading, DataValidationStatus validationStatus) {
        super(actualReading, validationStatus);
    }

    @Override
    public Optional<Range<Instant>> getRange() {
        return this.getActualReading().getTimePeriod();
    }

}