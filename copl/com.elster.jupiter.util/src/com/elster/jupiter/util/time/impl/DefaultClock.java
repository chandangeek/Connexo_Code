package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.Clock;

import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 27/05/13
 * Time: 15:41
 */
public class DefaultClock implements Clock {

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    @Override
    public Date now() {
        return new Date();
    }
}
