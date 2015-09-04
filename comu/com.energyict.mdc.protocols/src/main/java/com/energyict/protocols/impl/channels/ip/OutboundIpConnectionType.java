package com.energyict.protocols.impl.channels.ip;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Groups common behavior for outbound IP related connectionTypes.
 * <p>
 * Copyrights EnergyICT
 *
 * @since 9/11/12 (11:28)
 */
public abstract class OutboundIpConnectionType extends ConnectionTypeImpl {

    public static final String HOST_PROPERTY_NAME = "host";
    public static final String PORT_PROPERTY_NAME = "portNumber";
    public static final String CONNECTION_TIMEOUT_PROPERTY_NAME = "connectionTimeout";
    private static final TimeDuration DEFAULT_CONNECTION_TIMEOUT = TimeDuration.seconds(10);

    private final PropertySpecService propertySpecService;

    public OutboundIpConnectionType(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    private PropertySpec hostPropertySpec() {
        return this.propertySpecService.basicPropertySpec(HOST_PROPERTY_NAME, true, new StringFactory());
    }

    protected String hostPropertyValue() {
        return (String) this.getProperty(HOST_PROPERTY_NAME);
    }

    private PropertySpec portNumberPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(PORT_PROPERTY_NAME, true, new BigDecimalFactory());
    }

    private PropertySpec connectionTimeOutPropertySpec() {
        return  this.getPropertySpecService().basicPropertySpec(CONNECTION_TIMEOUT_PROPERTY_NAME, false, new TimeDurationValueFactory());
    }

    protected int portNumberPropertyValue() {
        return intProperty((BigDecimal) getProperty(PORT_PROPERTY_NAME));
    }

    protected int connectionTimeOutPropertyValue() {
        TimeDuration value = (TimeDuration) this.getProperty(CONNECTION_TIMEOUT_PROPERTY_NAME, DEFAULT_CONNECTION_TIMEOUT);
        return this.intProperty(value);
    }

    protected int intProperty(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        else {
            return value.intValue();
        }
    }

    protected int intProperty(TimeDuration value) {
        if (value == null) {
            return 0;
        }
        else {
            return (int) value.getMilliSeconds();
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.hostPropertySpec(),
                this.portNumberPropertySpec(),
                this.connectionTimeOutPropertySpec());
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