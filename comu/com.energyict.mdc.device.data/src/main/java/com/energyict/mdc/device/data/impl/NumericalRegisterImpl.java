package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.NumericalRegister;

import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.metering.ReadingRecord;

import java.util.List;

/**
 * Provides an implementation for the {@link NumericalRegister} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:50)
 */
public class NumericalRegisterImpl extends RegisterImpl<NumericalReading> implements NumericalRegister {

    public NumericalRegisterImpl(DeviceImpl device, RegisterSpec registerSpec) {
        super(device, registerSpec);
    }

    @Override
    protected NumericalReading newUnvalidatedReading(ReadingRecord actualReading) {
        return new NumericalReadingImpl(actualReading);
    }

    @Override
    protected NumericalReading newValidatedReading(ReadingRecord actualReading, List<ReadingQuality> readingQualities) {
        return new NumericalReadingImpl(actualReading, readingQualities);
    }

}