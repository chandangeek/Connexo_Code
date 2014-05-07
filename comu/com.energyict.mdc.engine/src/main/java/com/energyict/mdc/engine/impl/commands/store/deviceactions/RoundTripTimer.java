package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.util.time.Clock;

/**
 * A RoundTrip calculator. Start the timer before you do a read ({@link #start()}), stop it after you do a read ({@link #stop()}.
 * The total roundTrip time will be presented by {@link #getRoundTrip()}.
 */
public class RoundTripTimer {

    private final Clock clock;
    private long startTime;
    private long stopTime;

    public RoundTripTimer(Clock clock) {

        this.clock = clock;
    }

    /**
     * Start the 'timer'
     */
    public void start() {
        this.startTime = this.clock.now().getTime();
    }

    /**
     * Stop the 'timer'
     */
    public void stop() {
        this.stopTime = this.clock.now().getTime();
    }

    /**
     * Get the <b>TOTAL</b> RoundTripTime based on the Start- and StopTimes
     *
     * @return the calculated roundTripTime
     */
    public long getRoundTrip() {
        if (startTime == 0) {
            return 0;
        } else if (stopTime == 0) {
            return this.clock.now().getTime() - startTime;
        } else {
            return stopTime - startTime;
        }
    }

}