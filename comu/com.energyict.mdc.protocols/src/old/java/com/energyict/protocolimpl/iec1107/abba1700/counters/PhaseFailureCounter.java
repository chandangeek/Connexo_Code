/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 * The phaseFailureCounter keeps track of how many times a phaseFailure has occurred plus the latest three eventTimes and phase indication
 */
public class PhaseFailureCounter extends AbstractCounter{

    protected static final int PhaseIndicationLength = 1;
    private int firstFailedPhase;
    private int secondFailedPhase;
    private int thirdFailedPhase;

    /**
     * Create new instance
     *
     * @param protocolLink the used ProtocolLink
     */
    public PhaseFailureCounter(final ProtocolLink protocolLink) {
        super(protocolLink);
    }

    /**
     * Parse the date according to the counter specification
     *
     * @param data the data to parse
     * @throws java.io.IOException when the conversion of certain types did not succeed
     */
    @Override
    public void parse(final byte[] data) throws IOException {
        setCounter(ProtocolUtils.getIntLE(data, 0, CounterLength));
        long shift = (long) ProtocolUtils.getIntLE(data, CounterLength, DateTimeLength) & DateTimeMask;
        setMostRecentEventTime(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        shift = (long) ProtocolUtils.getIntLE(data, CounterLength + DateTimeLength, DateTimeLength) & DateTimeMask;
        setSecondMostRecentEventTime(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        shift = (long) ProtocolUtils.getIntLE(data, CounterLength + DateTimeLength + DateTimeLength, DateTimeLength) & DateTimeMask;
        setThirdMostRecentEventTime(ProtocolUtils.getCalendar(getTimeZone(), shift).getTime());
        setFirstFailedPhase(ProtocolUtils.getIntLE(data, CounterLength + DateTimeLength + DateTimeLength + DateTimeLength, PhaseIndicationLength));
        setSecondFailedPhase(ProtocolUtils.getIntLE(data, CounterLength + DateTimeLength + DateTimeLength + DateTimeLength + PhaseIndicationLength, PhaseIndicationLength));
        setThirdFailedPhase(ProtocolUtils.getIntLE(data, CounterLength + DateTimeLength + DateTimeLength + DateTimeLength + PhaseIndicationLength + PhaseIndicationLength, PhaseIndicationLength));
    }

    public void setFirstFailedPhase(final int firstFailedPhase) {
        this.firstFailedPhase = firstFailedPhase;
    }

    public void setSecondFailedPhase(final int secondFailedPhase) {
        this.secondFailedPhase = secondFailedPhase;
    }

    public void setThirdFailedPhase(final int thirdFailedPhase) {
        this.thirdFailedPhase = thirdFailedPhase;
    }

    public int getFirstFailedPhase() {
        return firstFailedPhase;
    }

    public int getSecondFailedPhase() {
        return secondFailedPhase;
    }

    public int getThirdFailedPhase() {
        return thirdFailedPhase;
    }
}
