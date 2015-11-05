package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
* Insert your comments here.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-08-13 (14:13)
*/
public class NoParamsConnectionType implements ConnectionType {

    private static final int HASH_CODE = 13469; // Random prime number

    @Override
    public boolean allowsSimultaneousConnections () {
        return false;
    }

    @Override
    public boolean supportsComWindow () {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes () {
        return EnumSet.allOf(ComPortType.class);
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        // No properties so nothing to copy here
    }

    @Override
    public Optional<CustomPropertySet<ConnectionType, ? extends PersistentDomainExtension<ConnectionType>>> getCustomPropertySet() {
        return Optional.empty();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return null;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public ConnectionType.Direction getDirection() {
        return ConnectionType.Direction.OUTBOUND;
    }

    @Override
    public String getVersion () {
        return "For testing purposes only";
    }

    @Override
    public int hashCode () {
        return HASH_CODE;
    }

    @Override
    public boolean equals (Object obj) {
        return obj instanceof NoParamsConnectionType || super.equals(obj);
    }

}