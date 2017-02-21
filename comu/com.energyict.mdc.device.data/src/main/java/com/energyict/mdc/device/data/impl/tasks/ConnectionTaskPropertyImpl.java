/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.impl.PluggableClassUsagePropertyImpl;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Provides an implementation for the {@link ConnectionTaskProperty} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-04 (09:05)
 */
public class ConnectionTaskPropertyImpl extends PluggableClassUsagePropertyImpl<ConnectionType> implements ConnectionTaskProperty {

    private final ConnectionTaskImpl connectionTask;

    public ConnectionTaskPropertyImpl(ConnectionTaskImpl connectionTask, String name) {
        super(name);
        this.connectionTask = connectionTask;
    }

    public ConnectionTaskPropertyImpl(ConnectionTaskImpl connectionTask, String name, Object value, Range<Instant> activePeriod, PluggableClass pluggableClass) {
        super(name, value, activePeriod, pluggableClass, true);
        this.connectionTask = connectionTask;
    }

    public boolean relatesTo(ConnectionTaskImpl connectionTask) {
        return this.connectionTask.getId() == connectionTask.getId();
    }

}