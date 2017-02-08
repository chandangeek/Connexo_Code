/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.protocols.impl.channels.ip.socket;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionType;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides an implementation for the {@link ConnectionType} interface for TLS demonstration.
 */
public class OutboundTlsConnectionType extends OutboundIpConnectionType {

    private final SocketService socketService;

    @Inject
    public OutboundTlsConnectionType(Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService) {
        super(propertySpecService, thesaurus);
        this.socketService = socketService;
    }

    protected SocketService getSocketService() {
        return socketService;
    }

    @Override
    public boolean allowsSimultaneousConnections () {
        return true;
    }

    @Override
    public boolean supportsComWindow () {
        return true;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes () {
        return EnumSet.of(ComPortType.TCP);
    }

    @Override
    public String getVersion () {
        return "$Date: 2013-04-12 15:03:44 +0200 (vr, 12 apr 2013) $";
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new OutboundTlsCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }

    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        this.copyProperties(properties);
        return this.newTcpIpConnection(this.socketService, this.hostPropertyValue(), this.portNumberPropertyValue(), this.connectionTimeOutPropertyValue());
    }

    protected void copyProperties(List<ConnectionProperty> properties) {
        properties
            .stream()
            .filter(this::hasValue)
            .forEach(this::copyProperty);
    }

    private boolean hasValue(ConnectionProperty property) {
        return property.getValue() != null;
    }

    private void copyProperty(ConnectionProperty property) {
        this.setProperty(property.getName(), property.getValue());
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for this OutboundTcpIpConnectionType
    }

}