package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.Clock;

import java.util.Date;
import java.util.TimeZone;

/**
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
