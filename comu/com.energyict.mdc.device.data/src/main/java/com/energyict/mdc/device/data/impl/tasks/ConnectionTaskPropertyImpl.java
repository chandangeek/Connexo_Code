package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.pluggable.PluggableClass;

/**
 * Provides an implementation for the {@link ConnectionTaskProperty} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-04 (09:05)
 */
public class ConnectionTaskPropertyImpl implements ConnectionTaskProperty {

    private PluggableClass pluggableClass;
    private String name;
    private Object value;
    private Interval activePeriod;
    private boolean inherited;

    public ConnectionTaskPropertyImpl (String name) {
        this.name = name;
    }

    public ConnectionTaskPropertyImpl (Relation relation, String name, PluggableClass pluggableClass) {
        this(name, relation.get(name), relation.getPeriod(), pluggableClass, false);
    }

    public ConnectionTaskPropertyImpl (String name, Object value, Interval activePeriod, PluggableClass pluggableClass) {
        this(name, value, activePeriod, pluggableClass, true);
    }

    public ConnectionTaskPropertyImpl(String name, Object value, Interval activePeriod, PluggableClass pluggableClass, boolean inherited) {
        this(name);
        this.value = value;
        this.activePeriod = activePeriod;
        this.pluggableClass = pluggableClass;
        this.inherited = inherited;
    }

    @Override
    public PluggableClass getPluggableClass () {
        return this.pluggableClass;
    }

    @Override
    public String getName () {
        return this.name;
    }

    @Override
    public Object getValue () {
        return this.value;
    }

    public void setValue (Object value) {
        this.value = value;
    }

    @Override
    public boolean isInherited () {
        return inherited;
    }

    @Override
    public Interval getActivePeriod () {
        return this.activePeriod;
    }

    public void setActivePeriod (Interval activePeriod) {
        this.activePeriod = activePeriod;
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append('(');
        builder.append(this.name);
        builder.append(':');
        builder.append(String.valueOf(this.value));
        if (this.inherited) {
            builder.append(", inherited");
        }
        if ((this.activePeriod.getStart() == null) && (this.activePeriod.getEnd() == null)) {
            builder.append(", always active");
        }
        else {
            builder.append(", active ");
            builder.append(String.valueOf(this.activePeriod));
        }
        builder.append(')');
        return builder.toString();
    }
}