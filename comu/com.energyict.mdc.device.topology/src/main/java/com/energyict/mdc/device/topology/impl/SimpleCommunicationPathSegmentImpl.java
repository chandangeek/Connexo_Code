/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;
import com.energyict.mdc.device.topology.SimpleCommunicationPathSegment;

/**
 * Provides an implementation for the {@link G3CommunicationPathSegment} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (14:42)
 */
public class SimpleCommunicationPathSegmentImpl extends CommunicationPathSegmentImpl implements SimpleCommunicationPathSegment {

    private int hopCount;

    SimpleCommunicationPathSegmentImpl createFrom(Device source, Device target, Interval interval, int hopCount) {
        super.init(source, target, interval);
        this.hopCount = hopCount;
        return this;
    }

    @Override
    public int getHopCount() {
        return this.hopCount;
    }

}