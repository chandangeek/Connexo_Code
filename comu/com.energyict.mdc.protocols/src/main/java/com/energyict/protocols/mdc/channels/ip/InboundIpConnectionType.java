package com.energyict.protocols.mdc.channels.ip;

import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.mdc.channels.VoidComChannel;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Groups common behavior for inbound IP related connectionTypes
 */
public class InboundIpConnectionType extends ConnectionTypeImpl {

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        // No properties
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return null;
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return false;
    }

    @Override
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.TCP, ComPortType.UDP);
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-10 17:15:01 +0200 (Mon, 10 Jun 2013) $";
    }

}