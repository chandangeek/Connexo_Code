/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.ObjectXmlMarshallAdapter;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.pluggable.PluggableClassUsageProperty;
import com.google.common.collect.Range;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

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
    private Range<Instant> activePeriod;
    private boolean inherited;

    public PluggableClassUsagePropertyImpl() {
        super();
    }

    public PluggableClassUsagePropertyImpl (String name) {
        super();
        this.name = name;
    }

    public PluggableClassUsagePropertyImpl (String name, Object value, Range<Instant> activePeriod, PluggableClass pluggableClass) {
        this(name, value, activePeriod, pluggableClass, false);
    }

    protected PluggableClassUsagePropertyImpl (String name, Object value, Range<Instant> activePeriod, PluggableClass pluggableClass, boolean inherited) {
        this(name);
        this.value = value;
        this.activePeriod = activePeriod;
        this.pluggableClass = pluggableClass;
        this.inherited = inherited;
    }

    @Override
    @XmlTransient
    public PluggableClass getPluggableClass () {
        return this.pluggableClass;
    }

    @Override
    @XmlAttribute
    public String getName () {
        return this.name;
    }

    @Override
    @XmlAttribute
    @XmlJavaTypeAdapter(ObjectXmlMarshallAdapter.class)
    public Object getValue () {
        return this.value;
    }

    public void setValue (Object value) {
        this.value = value;
    }

    @Override
    @XmlAttribute
    public boolean isInherited () {
        return inherited;
    }

    @Override
    @XmlTransient
    public Range<Instant> getActivePeriod() {
        return this.activePeriod;
    }

    public void setActivePeriod (Range<Instant> activePeriod) {
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
        if ((!this.activePeriod.hasLowerBound()) && (!this.activePeriod.hasUpperBound())) {
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