/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.ConnectionTypeImpl;
import com.energyict.protocols.impl.channels.VoidComChannel;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Inbound TCP connection type created for the CTR protocol base (as used by MTU155 and EK155 DeviceProtocols).<br>
 * Conform the CTR spec, this connectionType contains a required property for CallHomeId
 * since knocking devices are uniquely identified by their CallHomeID.
 */
public class CTRInboundDialHomeIdConnectionType extends ConnectionTypeImpl {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public CTRInboundDialHomeIdConnectionType(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new CTRInboundDialHomeIdCustomPropertySet(this.thesaurus, this.propertySpecService));
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
        return EnumSet.of(ComPortType.TCP);
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return new VoidComChannel();
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for this CTRInboundDialHomeIdConnectionType
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-06-26 15:15:49 +0200 (Mit, 26 Jun 2013) $";
    }

}