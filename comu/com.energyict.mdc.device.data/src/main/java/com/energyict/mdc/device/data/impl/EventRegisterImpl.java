package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.EventReading;
import com.energyict.mdc.device.data.EventRegister;

import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingRecord;

import java.util.List;

/**
 * Provides an implementation for the {@link EventRegister} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (13:50)
 */
public class EventRegisterImpl extends RegisterImpl<EventReading> implements EventRegister {

    public EventRegisterImpl(DeviceImpl device, RegisterSpec registerSpec) {
        super(device, registerSpec);
    }

    @Override
    protected EventReading newUnvalidatedReading(ReadingRecord actualReading) {
        return new EventReadingImpl(actualReading);
    }

    @Override
    protected EventReading newValidatedReading(ReadingRecord actualReading, List<ReadingQuality> readingQualities) {
        return new EventReadingImpl(actualReading, readingQualities);
    }

}