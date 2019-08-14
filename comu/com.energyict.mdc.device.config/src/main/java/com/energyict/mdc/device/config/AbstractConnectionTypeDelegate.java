/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.protocol.ConnectionProperty;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.exceptions.ConnectionException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract call that does nothing but delegate to the actual ConnectionType.
 * Serves as template for wrappers.
 */
abstract public class AbstractConnectionTypeDelegate implements ConnectionType {
    protected final ConnectionType connectionType;

    protected AbstractConnectionTypeDelegate(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return this.connectionType.getCustomPropertySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.connectionType.getPropertySpecs();
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return this.connectionType.allowsSimultaneousConnections();
    }

    @Override
    public boolean supportsComWindow() {
        return this.connectionType.supportsComWindow();
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return this.connectionType.getSupportedComPortTypes();
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        return this.connectionType.connect();
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        return this.connectionType.connect(properties);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        this.connectionType.disconnect(comChannel);
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return this.connectionType.getDirection();
    }

    @Override
    public String getVersion() {
        return this.connectionType.getVersion();
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.connectionType.copyProperties(properties);
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        return this.connectionType.getPropertySpec(name);
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return this.connectionType.getUPLPropertySpecs();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.connectionType.setUPLProperties(properties);

    }
}