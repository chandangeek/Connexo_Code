package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;

import javax.inject.Inject;

import java.time.Instant;

public class PassiveEffectiveCalendarImpl extends EffectiveCalendarImpl implements PassiveEffectiveCalendar{

    static final String TYPE_IDENTIFIER = "PEC";

    private Instant activationDate;

    @Override
    public Instant getActivationDate() {
        return activationDate;
    }

    @Inject
    public PassiveEffectiveCalendarImpl(AllowedCalendar calendar, Interval interval, Device device, Instant activationDate) {
        super(calendar, interval, device);
        this.activationDate = activationDate;
    }
}
