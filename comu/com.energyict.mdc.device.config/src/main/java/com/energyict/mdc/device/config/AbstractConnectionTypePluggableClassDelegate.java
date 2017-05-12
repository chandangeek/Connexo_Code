/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Abstract call that does nothing but delegate to the actual ConnectionTypePluggableClass.
 * Serves as template for wrappers
 */
abstract public class AbstractConnectionTypePluggableClassDelegate implements ConnectionTypePluggableClass {
    protected final ConnectionTypePluggableClass connectionTypePluggableClass;

    protected AbstractConnectionTypePluggableClassDelegate(ConnectionTypePluggableClass connectionTypePluggableClass) {
        this.connectionTypePluggableClass = connectionTypePluggableClass;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return connectionTypePluggableClass.getPropertySpecs();
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        return connectionTypePluggableClass.getPropertySpec(name);
    }

    @Override
    public String getVersion() {
        return connectionTypePluggableClass.getVersion();
    }

    @Override
    public ConnectionType getConnectionType() {
        return connectionTypePluggableClass.getConnectionType();
    }

    @Override
    public boolean isInstance(ConnectionType connectionType) {
        return connectionTypePluggableClass.isInstance(connectionType);
    }

    @Override
    public CustomPropertySetValues getPropertiesFor(ConnectionProvider connectionProvider, Instant effectiveTimestamp) {
        return connectionTypePluggableClass.getPropertiesFor(connectionProvider, effectiveTimestamp);
    }

    @Override
    public void setPropertiesFor(ConnectionProvider connectionProvider, CustomPropertySetValues value, Instant effectiveTimestamp) {
        connectionTypePluggableClass.setPropertiesFor(connectionProvider, value, effectiveTimestamp);
    }

    @Override
    public void removePropertiesFor(ConnectionProvider connectionProvider) {
        connectionTypePluggableClass.removePropertiesFor(connectionProvider);
    }

    @Override
    public int getNrOfRetries() {
        return connectionTypePluggableClass.getNrOfRetries();
    }

    @Override
    public void updateNrOfRetries(int nrOfRetries) {
        connectionTypePluggableClass.updateNrOfRetries(nrOfRetries);
    }

    @Override
    public void setName(String name) {
        connectionTypePluggableClass.setName(name);
    }

    @Override
    public PluggableClassType getPluggableClassType() {
        return connectionTypePluggableClass.getPluggableClassType();
    }

    @Override
    public String getJavaClassName() {
        return connectionTypePluggableClass.getJavaClassName();
    }

    @Override
    public Instant getModificationDate() {
        return connectionTypePluggableClass.getModificationDate();
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return connectionTypePluggableClass.getProperties(propertySpecs);
    }

    @Override
    public void setProperty(PropertySpec propertySpec, Object value) {
        connectionTypePluggableClass.setProperty(propertySpec, value);
    }

    @Override
    public void removeProperty(PropertySpec propertySpec) {
        connectionTypePluggableClass.removeProperty(propertySpec);
    }

    @Override
    public void save() {
        connectionTypePluggableClass.save();
    }

    @Override
    public void delete() {
        connectionTypePluggableClass.delete();
    }

    @Override
    public long getEntityVersion() {
        return connectionTypePluggableClass.getEntityVersion();
    }

    @Override
    public long getId() {
        return connectionTypePluggableClass.getId();
    }

    @Override
    public String getName() {
        return connectionTypePluggableClass.getName();
    }
}
