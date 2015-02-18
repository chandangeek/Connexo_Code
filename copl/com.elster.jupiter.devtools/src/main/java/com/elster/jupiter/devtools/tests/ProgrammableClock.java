package com.elster.jupiter.devtools.tests;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Iterator;

public class ProgrammableClock extends Clock {

    private Iterator<Instant> instants;
    private Instant last;
    private ZoneId zoneId;

    public ProgrammableClock(ZoneId zoneId, Instant first, Instant... subsequent) {
        this.last = first;
        instants = Arrays.asList(subsequent).iterator();
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

    @Override
    public Instant instant() {
        try {
            return last;
        } finally {
            if (instants.hasNext()) {
                last = instants.next();
            }
        }
    }
}
