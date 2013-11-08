package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * The PowerDownCounter keeps track of how many times the powerDown event has occurred plus the lates three eventTimes
 */
public class PowerDownCounter extends AbstractCounter {

    /**
     * Create new instance
     *
     * @param protocolLink the used ProtocolLink
     */
    public PowerDownCounter(final ProtocolLink protocolLink) {
        super(protocolLink);
    }
}
