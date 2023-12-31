/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.common.protocol.ConnectionProperty;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (16:05)
 */
public class MockOutboundConnectionType implements ConnectionType {
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
        return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::adaptTo).collect(Collectors.toList());
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
        return Collections.emptySet();
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        return null;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public ConnectionTypeDirection getDirection() {
        return ConnectionTypeDirection.OUTBOUND;
    }

    @Override
    public String getVersion() {
        return "1.0.0-SNAPSHOT";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.empty();
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