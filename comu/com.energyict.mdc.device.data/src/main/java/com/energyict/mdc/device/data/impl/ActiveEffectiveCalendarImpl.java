package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;

public class ActiveEffectiveCalendarImpl extends EffectiveCalendarImpl implements ActiveEffectiveCalendar {
    static final String TYPE_IDENTIFIER = "AEC";

    @Inject
    public ActiveEffectiveCalendarImpl (AllowedCalendar calendar, Interval interval, Device device) {
        super(calendar, interval, device);
    }
}
