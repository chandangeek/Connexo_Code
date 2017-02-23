package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.protocol.exceptions.ConnectionException;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
* Insert your comments here.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-08-13 (14:13)
*/
public abstract class NoParamsConnectionType implements ConnectionType {

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
    public ComChannel connect() throws ConnectionException {
        return null;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::new).collect(Collectors.toList());
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

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
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.empty();
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return null;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
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