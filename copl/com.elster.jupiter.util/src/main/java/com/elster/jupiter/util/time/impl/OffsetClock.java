package com.elster.jupiter.util.time.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

class OffsetClock extends Clock {

    private final Clock baseClock; // clock to recalculate offset against
    private volatile Clock usingClock; // clock to delegate calls to, and to redefine when changing offsets

    public OffsetClock(Clock baseClock, LocalDateTime newNow) {
        this.baseClock = baseClock;
        usingClock = Clock.offset(baseClock, Duration.between(ZonedDateTime.now(baseClock), newNow.atZone(baseClock.getZone())));
    }

    public OffsetClock(Clock baseClock) {
        this.baseClock = baseClock;
        usingClock = baseClock;
    }

    public static Clock systemUTC() {
        return Clock.systemUTC();
    }

    @Override
    public ZoneId getZone() {
        return usingClock.getZone();
    }

    @Override
    public Instant instant() {
        return usingClock.instant();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        if (getZone().equals(zone)) {
            return this;
        }
        return new OffsetClock(baseClock.withZone(zone), ZonedDateTime.now(zone).toLocalDateTime());
    }

    public void set(LocalDateTime newNow) {
        usingClock = Clock.offset(baseClock, Duration.between(ZonedDateTime.now(baseClock), newNow.atZone(baseClock.getZone())));
    }
}
