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
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLConnectionTypeAdapter;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract call that does nothing but delegate to the actual ConnectionType.
 * Serves as template for wrappers.
 */
abstract public class AbstractConnectionTypeDelegate implements ConnectionType {

    private ConnectionType innerConnectionType;

    private ConnectionTypeDirection direction;

    protected AbstractConnectionTypeDelegate() {
        super();
    }

    protected AbstractConnectionTypeDelegate(ConnectionType connectionType) {
        this.innerConnectionType = connectionType;
    }

    @Override
    @XmlTransient
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return this.innerConnectionType.getCustomPropertySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.innerConnectionType.getPropertySpecs();
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return this.innerConnectionType.allowsSimultaneousConnections();
    }

    @Override
    public boolean enableHHUSignOn() {
        return this.innerConnectionType.enableHHUSignOn();
    }

    @Override
    public boolean supportsComWindow() {
        return this.innerConnectionType.supportsComWindow();
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return this.innerConnectionType.getSupportedComPortTypes();
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        return this.innerConnectionType.connect();
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        return this.innerConnectionType.connect(properties);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        this.innerConnectionType.disconnect(comChannel);
    }

    @Override
    @XmlAttribute
    public ConnectionTypeDirection getDirection() {
        if (innerConnectionType != null) {
            direction = this.innerConnectionType.getDirection();
        }
        return direction;
    }

    @Override
    public String getVersion() {
        return this.innerConnectionType.getVersion();
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.innerConnectionType.copyProperties(properties);
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        return this.innerConnectionType.getPropertySpec(name);
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return this.innerConnectionType.getUPLPropertySpecs();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.innerConnectionType.setUPLProperties(properties);

    }

    @XmlElements({
            @XmlElement(type = UPLConnectionTypeAdapter.class),
    })
    protected ConnectionType getInnerConnectionType() {
        return innerConnectionType;
    }

    protected void setInnerConnectionType(ConnectionType innerConnectionType) {
        this.innerConnectionType = innerConnectionType;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }
}