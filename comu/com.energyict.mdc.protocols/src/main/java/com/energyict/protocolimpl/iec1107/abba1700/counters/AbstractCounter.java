package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Abstract class for <i>Counter</i> parsing.
 * Ex. the Programming Counter (id 680), the PhaseFailure Counter (id 693), the ReverseRun Counter (id 694), the PowerDown counter (id 695)
 */
public abstract class AbstractCounter {

    protected static final int CounterLength = 2;
    protected static final int DateTimeLength = 4;
    protected static final long DateTimeMask = 0xFFFFFFFFL;
    protected final ProtocolLink protocolLink;
    private Date mostRecentEventTime;
    private Date secondMostRecentEventTime;
    private Date thirdMostRecentEventTime;
    private int counter;

    /**
     * Create new instance
     *
     * @param protocolLink the used ProtocolLink
     */
    public AbstractCounter(final ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }

    /**
     * Parse the date according to the counter specification
     *
     * @param data the data to parse
     * @throws IOException when the conversion of certain types did not succeed
     */
    public void parse(byte[] data) throws IOException {
        setCounter(ProtocolUtils.getIntLE(data, 0, CounterLength));
        long shift = (long) ProtocolUtils.getIntLE(data, CounterLength, DateTimeLength) & DateTimeMask;
        setMostRecentEventTime(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        shift = (long) ProtocolUtils.getIntLE(data, CounterLength + DateTimeLength, DateTimeLength) & DateTimeMask;
        setSecondMostRecentEventTime(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        shift = (long) ProtocolUtils.getIntLE(data, CounterLength + DateTimeLength + DateTimeLength, DateTimeLength) & DateTimeMask;
        setThirdMostRecentEventTime(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
    }

    protected void setMostRecentEventTime(final Date time) {
        this.mostRecentEventTime = time;
    }

    protected void setSecondMostRecentEventTime(final Date time) {
        this.secondMostRecentEventTime = time;
    }

    protected void setThirdMostRecentEventTime(final Date time) {
        this.thirdMostRecentEventTime = time;
    }

    /**
     * Setter for the counter
     *
     * @param counter the counter to set
     */
    protected void setCounter(final int counter) {
        this.counter = counter;
    }

    /**
     * Getter for the timeZone to use to set the dates
     */
    protected TimeZone getTimeZone() {
        return this.protocolLink.getTimeZone();
    }

    public int getCounter() {
        return counter;
    }

    public Date getMostRecentEventTime() {
        return mostRecentEventTime;
    }

    public Date getSecondMostRecentEventTime() {
        return secondMostRecentEventTime;
    }

    public Date getThirdMostRecentEventTime() {
        return thirdMostRecentEventTime;
    }
}
