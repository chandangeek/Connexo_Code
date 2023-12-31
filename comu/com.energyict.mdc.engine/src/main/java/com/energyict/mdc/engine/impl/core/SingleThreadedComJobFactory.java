/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.comserver.OutboundComPort;

/**
 * Provides an implementation for the {@link ComJobFactory} that
 * is intended for single threaded {@link OutboundComPort},
 * i.e. ComPorts that have only 1 simultaneous connections.
 */
public final class SingleThreadedComJobFactory extends GroupingComJobFactory {

    public SingleThreadedComJobFactory() {
        super(1);
    }

    @Override
    protected boolean continueFetchingOnNewConnectionTask() {
        return false;
    }
}