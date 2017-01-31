/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.data.TextReading;
import com.energyict.mdc.device.data.TextRegister;

/**
 * Provides an implementation for the {@link TextRegister} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:50)
 */
public class TextRegisterImpl extends RegisterImpl<TextReading, TextualRegisterSpec> implements TextRegister {

    public TextRegisterImpl(DeviceImpl device, TextualRegisterSpec registerSpec) {
        super(device, registerSpec);
    }

    @Override
    protected TextReading newUnvalidatedReading(ReadingRecord actualReading) {
        return new TextReadingImpl(actualReading);
    }

    @Override
    protected TextReading newValidatedReading(ReadingRecord actualReading, DataValidationStatus validationStatus) {
        return new TextReadingImpl(actualReading, validationStatus);
    }

}