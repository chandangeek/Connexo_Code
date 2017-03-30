/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * The ReverseRun counter keeps track of how many times a reverseRun event has occurred plus the latest three eventTimes
 */
public class ReverseRunCounter extends AbstractCounter{

    /**
     * Create new instance
     *
     * @param protocolLink the used ProtocolLink
     */
    public ReverseRunCounter(final ProtocolLink protocolLink) {
        super(protocolLink);
    }
}
