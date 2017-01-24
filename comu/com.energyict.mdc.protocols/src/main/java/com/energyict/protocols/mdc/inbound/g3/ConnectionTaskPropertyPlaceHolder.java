package com.energyict.protocols.mdc.inbound.g3;

import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.pluggable.PluggableClass;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Dummy implementations that simply holds the name and value.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/06/2015 - 13:00
 */
public class ConnectionTaskPropertyPlaceHolder implements ConnectionTaskProperty {

    private String name;
    private Object value;
    private Range<Instant> timePeriod;

    public ConnectionTaskPropertyPlaceHolder(String propertyName, Object property, Range<Instant> timePeriod) {
        this.name = propertyName;
        this.value = property;
        this.timePeriod = timePeriod;
    }

    @Override
    public PluggableClass getPluggableClass() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean isInherited() {
        return false;
    }

    @Override
    public Range<Instant> getActivePeriod() {
        return timePeriod;
    }
}