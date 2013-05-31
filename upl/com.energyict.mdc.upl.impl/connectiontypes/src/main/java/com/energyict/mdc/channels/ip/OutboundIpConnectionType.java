package com.energyict.mdc.channels.ip;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Groups common behavior for outbound IP related connectionTypes
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 11:28
 */
public abstract class OutboundIpConnectionType extends ConnectionTypeImpl {

    public static final String HOST_PROPERTY_NAME = "host";
    public static final String PORT_PROPERTY_NAME = "portNumber";
    public static final String CONNECTION_TIMEOUT_PROPERTY_NAME = "connectionTimeout";

    private PropertySpec hostPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(HOST_PROPERTY_NAME);
    }

    protected String hostPropertyValue() {
        return (String) this.getProperty(HOST_PROPERTY_NAME);
    }

    private PropertySpec portNumberPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(PORT_PROPERTY_NAME);
    }

    private PropertySpec connectionTimeOutPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(CONNECTION_TIMEOUT_PROPERTY_NAME);
    }

    protected int portNumberPropertyValue() {
        BigDecimal value = (BigDecimal) this.getProperty(PORT_PROPERTY_NAME);
        return this.intProperty(value);
    }

    protected int connectionTimeOutPropertyValue() {
        BigDecimal value = (BigDecimal) this.getProperty(CONNECTION_TIMEOUT_PROPERTY_NAME);
        return this.intProperty(value);
    }

    protected int intProperty(BigDecimal value) {
        if (value == null) {
            return 0;
        } else {
            return value.intValue();
        }
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
    public boolean isRequiredProperty(String name) {
        return HOST_PROPERTY_NAME.equals(name) || PORT_PROPERTY_NAME.equals(name);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> requiredProperties = new ArrayList<>(2);
        requiredProperties.add(this.hostPropertySpec());
        requiredProperties.add(this.portNumberPropertySpec());
        return requiredProperties;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optional = new ArrayList<>();
        optional.add(this.connectionTimeOutPropertySpec());
        return optional;
    }
}
