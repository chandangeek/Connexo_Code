/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Provides an implementation for the {@link ComJobFactory} that
 * is intended for multi threaded {@link com.energyict.mdc.engine.config.OutboundComPort},
 * i.e. ComPorts that have only more than 1 simultaneous connections.
 */
public final class MultiThreadedComJobFactory extends GroupingComJobFactory {

    public MultiThreadedComJobFactory(int maximumJobs) {
        super(maximumJobs);
    }

    @Override
    protected boolean continueFetchingOnNewConnectionTask () {
        return this.numberOfJobs() < this.getMaximumJobs();
    }

}
