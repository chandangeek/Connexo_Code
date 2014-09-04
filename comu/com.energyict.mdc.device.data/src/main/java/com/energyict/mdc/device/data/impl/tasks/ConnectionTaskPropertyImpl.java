package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.device.data.impl.PluggableClassUsagePropertyImpl;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.pluggable.PluggableClass;

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

    public ConnectionTaskPropertyImpl(ConnectionTaskImpl connectionTask, Relation relation, String name, PluggableClass pluggableClass) {
        super(relation, name, pluggableClass);
        this.connectionTask = connectionTask;
    }

    public ConnectionTaskPropertyImpl(ConnectionTaskImpl connectionTask, String name, Object value, Interval activePeriod, PluggableClass pluggableClass) {
        super(name, value, activePeriod, pluggableClass, true);
        this.connectionTask = connectionTask;
    }

    public boolean relatesTo(ConnectionTaskImpl connectionTask) {
        return this.connectionTask.getId() == connectionTask.getId();
    }

}