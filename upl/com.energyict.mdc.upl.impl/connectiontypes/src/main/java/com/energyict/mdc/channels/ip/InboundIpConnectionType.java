package com.energyict.mdc.channels.ip;

import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.VoidComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTypeImpl;

import com.energyict.cpo.PropertySpec;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Groups common behavior for inbound IP related connectionTypes
 */
@XmlRootElement
public class InboundIpConnectionType extends ConnectionTypeImpl {


    @Override
    public PropertySpec getPropertySpec(String name) {
        return null;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return false;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
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
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.INBOUND;
    }
}
