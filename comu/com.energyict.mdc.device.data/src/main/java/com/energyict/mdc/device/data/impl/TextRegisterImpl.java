package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.TextReading;
import com.energyict.mdc.device.data.TextRegister;

import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingRecord;

import java.util.List;

/**
 * Provides an implementation for the {@link TextRegister} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:50)
 */
public class TextRegisterImpl extends RegisterImpl<TextReading> implements TextRegister {

    public TextRegisterImpl(DeviceImpl device, RegisterSpec registerSpec) {
        super(device, registerSpec);
    }

    @Override
    protected TextReading newUnvalidatedReading(ReadingRecord actualReading) {
        return new TextReadingImpl(actualReading);
    }

    @Override
    protected TextReading newValidatedReading(ReadingRecord actualReading, List<ReadingQuality> readingQualities) {
        return new TextReadingImpl(actualReading, readingQualities);
    }

}