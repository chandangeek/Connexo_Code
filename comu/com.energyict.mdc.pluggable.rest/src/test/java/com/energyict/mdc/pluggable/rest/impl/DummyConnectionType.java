package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.protocol.exceptions.ConnectionException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (15:15)
 */
public abstract class DummyConnectionType implements ConnectionType {

    @Override
    public boolean allowsSimultaneousConnections() {
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
    public boolean supportsComWindow() {
        return false;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return null;
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        return null;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {

    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.empty();
    }
}