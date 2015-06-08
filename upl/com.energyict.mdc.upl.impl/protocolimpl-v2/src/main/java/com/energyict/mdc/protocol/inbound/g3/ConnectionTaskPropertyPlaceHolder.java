package com.energyict.mdc.protocol.inbound.g3;

import com.energyict.cbo.TimePeriod;
import com.energyict.mdc.pluggable.PluggableClassWithInquirySupport;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

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
    private TimePeriod timePeriod;

    public ConnectionTaskPropertyPlaceHolder(String propertyName, Object property, TimePeriod timePeriod) {
        this.name = propertyName;
        this.value = property;
        this.timePeriod = timePeriod;
    }

    @Override
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
    }

    @Override
    public PluggableClassWithInquirySupport getPluggableClass() {
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
    public TimePeriod getActivePeriod() {
        return timePeriod;
    }
}