package com.energyict.mdc.channels.ip.datagrams;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.channels.ip.OutboundIpConnectionType;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import java.math.BigDecimal;
import java.util.*;

/**
 * Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType} interface for UDP.
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 11:20
 */
public class OutboundUdpConnectionType extends OutboundIpConnectionType {

    public static final String BUFFER_SIZE_NAME = "udpdatagrambuffersize";

    public OutboundUdpConnectionType() {
        super();
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return true;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.UDP);
    }

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        for (ConnectionTaskProperty property : properties) {
            if(property.getValue() != null){
                this.setProperty(property.getName(), property.getValue());
            }
        }
        return this.newUDPConnection(this.getBufferSizePropertyValue(), this.hostPropertyValue(), this.portNumberPropertyValue());
    }

    private int getBufferSizePropertyValue() {
        BigDecimal value = (BigDecimal) this.getProperty(BUFFER_SIZE_NAME);
        return this.intProperty(value);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        final List<PropertySpec> allRequiredProperties = super.getRequiredProperties();
        allRequiredProperties.add(this.bufferSizePropertySpec());
        return allRequiredProperties;
    }

    private PropertySpec bufferSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(BUFFER_SIZE_NAME);
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return super.isRequiredProperty(name) || BUFFER_SIZE_NAME.equals(name);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        PropertySpec superPropertySpec = super.getPropertySpec(name);
        if(superPropertySpec != null){
            return superPropertySpec;
        } else if(BUFFER_SIZE_NAME.equals(name)) {
            return this.bufferSizePropertySpec();
        } else {
            return null;
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-04-12 15:03:44 +0200 (vr, 12 apr 2013) $";
    }
}
