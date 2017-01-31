/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.optical.dlms;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.ConnectionTypeServiceImpl;
import com.energyict.protocols.impl.channels.ConnectionTypeImpl;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LegacyOpticalDlmsConnectionType extends ConnectionTypeImpl {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;
    private final SioOpticalConnectionType actualConnectionType;

    @Inject
    public LegacyOpticalDlmsConnectionType(@Named(ConnectionTypeServiceImpl.SERIAL_PLAIN_GUICE_INJECTION_NAME) SerialComponentService serialComponentService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.actualConnectionType = new SioOpticalConnectionType(serialComponentService, thesaurus);
    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return this.actualConnectionType.allowsSimultaneousConnections();
    }

    @Override
    public boolean supportsComWindow() {
        return this.actualConnectionType.supportsComWindow();
    }

    @Override
    public Set<ComPortType> getSupportedComPortTypes() {
        return this.actualConnectionType.getSupportedComPortTypes();
    }

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        return this.actualConnectionType.connect(properties);
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        this.actualConnectionType.disconnect(comChannel);
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public Optional<CustomPropertySet<ConnectionProvider, ? extends PersistentDomainExtension<ConnectionProvider>>> getCustomPropertySet() {
        return Optional.of(new LegacyOpticalDlmsCustomPropertySet(this.thesaurus, this.actualConnectionType, this.propertySpecService));
    }

}