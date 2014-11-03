package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Groups common behavior for outbound IP related connectionTypes.
 *
 * Copyrights EnergyICT
 * @since 9/11/12 (11:28)
 */
public abstract class OutboundIpConnectionType extends ConnectionTypeImpl {

    public static final String HOST_PROPERTY_NAME = "host";
    public static final String PORT_PROPERTY_NAME = "portNumber";
    public static final String CONNECTION_TIMEOUT_PROPERTY_NAME = "connectionTimeout";

    private PropertySpec hostPropertySpec() {
        return RequiredPropertySpecFactory.newInstance().stringPropertySpec(HOST_PROPERTY_NAME);
    }

    protected String hostPropertyValue() {
        return (String) this.getProperty(HOST_PROPERTY_NAME);
    }

    private PropertySpec portNumberPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(PORT_PROPERTY_NAME, true, new BigDecimalFactory());
    }

    private PropertySpec connectionTimeOutPropertySpec() {
        return Bus.getPropertySpecService().basicPropertySpec(CONNECTION_TIMEOUT_PROPERTY_NAME, false, new TimeDurationValueFactory());
    }

    protected int portNumberPropertyValue() {
        BigDecimal value = (BigDecimal) this.getProperty(PORT_PROPERTY_NAME);
        return this.intProperty(value);
    }

    protected int connectionTimeOutPropertyValue() {
        TimeDuration value = (TimeDuration) this.getProperty(CONNECTION_TIMEOUT_PROPERTY_NAME);
        return this.intProperty(value);
    }

    protected int intProperty(BigDecimal value) {
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
    }

    protected int intProperty(TimeDuration value) {
        if (value == null) {
            return 0;
        } else {
            return (int) value.getMilliSeconds();
        }
    }

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        propertySpecs.add(this.hostPropertySpec());
        propertySpecs.add(this.portNumberPropertySpec());
        propertySpecs.add(this.connectionTimeOutPropertySpec());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name) {
            case HOST_PROPERTY_NAME:
                return this.hostPropertySpec();
            case PORT_PROPERTY_NAME:
                return this.portNumberPropertySpec();
            case CONNECTION_TIMEOUT_PROPERTY_NAME:
                return this.connectionTimeOutPropertySpec();
            default:
                return null;
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }
}