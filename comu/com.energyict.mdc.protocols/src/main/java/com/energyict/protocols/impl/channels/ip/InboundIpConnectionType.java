package com.energyict.protocols.impl.channels.ip;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.protocols.impl.channels.VoidComChannel;
import com.energyict.protocols.mdc.protocoltasks.ConnectionTypeImpl;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Groups common behavior for inbound IP related connectionTypes
 */
public class InboundIpConnectionType extends ConnectionTypeImpl {

    @Override
    public Optional<CustomPropertySet<ConnectionType, ? extends PersistentDomainExtension<ConnectionType>>> getCustomPropertySet() {
        return Optional.empty();
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
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for this InboundIpConnectionType
    }

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-10 17:15:01 +0200 (Mon, 10 Jun 2013) $";
    }

}