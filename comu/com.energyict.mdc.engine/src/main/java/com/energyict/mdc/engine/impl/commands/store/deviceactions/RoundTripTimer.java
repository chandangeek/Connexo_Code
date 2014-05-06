package com.energyict.mdc.engine.impl.commands.store.deviceactions;

/**
 * A RoundTrip calculator. Start the timer before you do a read ({@link #start()}), stop it after you do a read ({@link #stop()}.
 * The total roundTrip time will be presented by {@link #getRoundTrip()}.
 */
public class RoundTripTimer {

    private long startTime;
    private long stopTime;

    /**
     * Start the 'timer'
     */
    public void start() {
        this.startTime = MeteringWarehouse.getCurrent().getClock().now().getTime();
    }

    /**
     * Stop the 'timer'
     */
    public void stop() {
        this.stopTime = MeteringWarehouse.getCurrent().getClock().now().getTime();
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
            return MeteringWarehouse.getCurrent().getClock().now().getTime() - startTime;
        } else {
            return stopTime - startTime;
        }
    }

}