/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectProperty;

import com.google.common.collect.Range;

import java.time.Instant;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link DeviceProtocolDialectProperty} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-04 (09:05)
 */
public class DeviceProtocolDialectPropertyImpl extends PluggableClassUsagePropertyImpl<DeviceProtocolDialect> implements DeviceProtocolDialectProperty {

    static final int HASHCODE_PRIME = 31;

    public DeviceProtocolDialectPropertyImpl(String name) {
        super(name);
        this.setActivePeriod(Range.all());
    }

    public DeviceProtocolDialectPropertyImpl(String name, Object value, Range<Instant> activePeriod, PluggableClass pluggableClass) {
        super(name, value, activePeriod, pluggableClass);
    }

    protected DeviceProtocolDialectPropertyImpl (String name, Object value, Range<Instant> activePeriod, PluggableClass pluggableClass, boolean inherited) {
        super(name, value, activePeriod, pluggableClass, inherited);
    }

    @Override
    public boolean equals (Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return this.doEquals((DeviceProtocolDialectPropertyImpl) other);
    }

    private boolean doEquals(DeviceProtocolDialectPropertyImpl otherDialectProperty) {
        return this.getName().equals(otherDialectProperty.getName())
                && is(this.getValue()).equalTo(otherDialectProperty.getValue())
                && is(this.getPluggableClass()).equalTo(otherDialectProperty.getPluggableClass())
                && is(this.getActivePeriod()).equalTo(otherDialectProperty.getActivePeriod())
                && is(this.isInherited()).equalTo(otherDialectProperty.isInherited());
    }

    @Override
    public int hashCode () {
        int result = this.getName().hashCode();
        result = HASHCODE_PRIME * result + (this.getValue() != null ? this.getValue().hashCode() : 0);
        result = HASHCODE_PRIME * result + (this.getPluggableClass() != null ? this.getPluggableClass().hashCode() : 0);
        result = HASHCODE_PRIME * result + (this.getActivePeriod() != null ? this.getActivePeriod().hashCode() : 0);
        result = HASHCODE_PRIME * result + (this.isInherited() ? 1 : 0);
        return result;
    }

}