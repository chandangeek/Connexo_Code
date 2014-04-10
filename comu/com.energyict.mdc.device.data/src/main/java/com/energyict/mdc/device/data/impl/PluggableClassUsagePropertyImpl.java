package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.dynamic.HasDynamicProperties;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassUsageProperty;

/**
 * Provides an implementation for the {@link PluggableClassUsageProperty} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-13 (08:53)
 */
public class PluggableClassUsagePropertyImpl<T extends HasDynamicProperties> implements PluggableClassUsageProperty<T> {

    private PluggableClass pluggableClass;
    private String name;
    private Object value;
    private Interval activePeriod;
    private boolean inherited;

    public PluggableClassUsagePropertyImpl (String name) {
        super();
        this.name = name;
    }

    public PluggableClassUsagePropertyImpl (Relation relation, String name, PluggableClass pluggableClass) {
        this(name, relation.get(name), relation.getPeriod(), pluggableClass, false);
    }

    protected PluggableClassUsagePropertyImpl (String name, Object value, Interval activePeriod, PluggableClass pluggableClass, boolean inherited) {
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