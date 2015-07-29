package com.elster.jupiter.devtools.tests;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

public class ProgrammableClock extends Clock {

    private Iterator<Instant> instants;
    private Supplier<Instant> ticker;
    private Instant last;
    private ZoneId zoneId;

    public ProgrammableClock(ZoneId zoneId, Instant first, Instant... subsequent) {
        this.last = first;
        instants = Arrays.asList(subsequent).iterator();
        this.zoneId = zoneId;
        this.ticker = null;
    }

    public ProgrammableClock(ZoneId zoneId, Supplier<Instant> ticker) {
        this.last = ticker.get();
        this.ticker = ticker;
        instants = null;
        this.zoneId = zoneId;
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new Clock() {
            @Override
            public ZoneId getZone() {
                return zone;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return ProgrammableClock.this.withZone(zone);
            }

            @Override
            public Instant instant() {
                return ProgrammableClock.this.instant();
            }
        };
    }

    public void setSubsequent(Instant... subsequent) {
        instants = Arrays.asList(subsequent).iterator();
    }

    public void setTicker(Supplier<Instant> ticker) {
        this.ticker = ticker;
    }

    @Override
    public Instant instant() {
        try {
            return last;
        } finally {
            if (ticker != null) {
                last = ticker.get();
            } else if (instants.hasNext()) {
                last = instants.next();
            }
        }
    }
}
