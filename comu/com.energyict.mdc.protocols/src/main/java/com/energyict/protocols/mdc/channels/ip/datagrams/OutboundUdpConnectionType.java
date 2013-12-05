package com.energyict.protocols.mdc.channels.ip.datagrams;

import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.RequiredPropertySpecFactory;
import com.energyict.protocols.mdc.channels.ip.OutboundIpConnectionType;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link ConnectionType} interface for UDP.
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
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        for (ConnectionProperty property : properties) {
            if (property.getValue() != null) {
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
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        super.addPropertySpecs(propertySpecs);
        propertySpecs.add(this.bufferSizePropertySpec());
    }

    private PropertySpec bufferSizePropertySpec() {
        return RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec(BUFFER_SIZE_NAME);
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