package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * The ProgrammingCounter contains the number of times the meter has changed his config, plus the last three times this change has occurred
 */
public class ProgrammingCounter extends AbstractCounter {

    public ProgrammingCounter(final ProtocolLink protocolLink) {
        super(protocolLink);
    }

}
