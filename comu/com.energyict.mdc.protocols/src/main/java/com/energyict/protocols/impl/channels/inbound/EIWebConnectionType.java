/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.ServerConnectionType;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EIWebConnectionType implements ServerConnectionType {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public EIWebConnectionType(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return true;
    }

    @Override
    public boolean supportsComWindow() {
        return true;
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return EnumSet.of(ComPortType.SERVLET);
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        throw new UnsupportedOperationException("Calling connect is not allowed on an EIWebConnectionType");
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
    }

    @Override
    public Direction getDirection() {
        return Direction.INBOUND;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-09-10 15:38:19 +0200 (Tue, 10 Sep 2013) $";
    }

    @Override
    public void copyProperties (TypedProperties properties) {
        // No implementation needed
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new EIWebCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

}