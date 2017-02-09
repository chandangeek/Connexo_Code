package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.TextReading;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;

/**
 * Provides an implementation for the {@link TextReading} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (15:38)
 */
public class TextReadingImpl extends ReadingImpl implements TextReading {

    protected TextReadingImpl(ReadingRecord actualReading, Register<?, ?> register, ReadingRecord previousReading) {
        super(actualReading, register, previousReading);
    }

    protected TextReadingImpl(ReadingRecord actualReading, DataValidationStatus validationStatus, Register<?, ?> register, ReadingRecord previousReading) {
        super(actualReading, validationStatus, register, previousReading);
    }

    @Override
    public String getValue() {
        return this.getActualReading().getText();
    }

}