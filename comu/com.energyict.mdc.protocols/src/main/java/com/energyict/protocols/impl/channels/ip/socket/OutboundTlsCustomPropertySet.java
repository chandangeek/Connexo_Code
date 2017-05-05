/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.protocols.impl.channels.ip.socket;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.upl.crypto.KeyStoreService;
import com.energyict.mdc.upl.crypto.X509Service;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.protocols.impl.channels.AbstractConnectionTypeCustomPropertySet;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionProperties;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionPropertiesPersistenceSupport;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link com.energyict.mdc.channels.ip.socket.TLSConnectionType}.
 */
public class OutboundTlsCustomPropertySet  extends AbstractConnectionTypeCustomPropertySet implements CustomPropertySet<ConnectionProvider, OutboundIpConnectionProperties> {

    private final NlsService nlsService;
    private final X509Service x509Service;
    private final KeyStoreService keyStoreService;

    @Inject
    public OutboundTlsCustomPropertySet(Thesaurus thesaurus, com.energyict.mdc.upl.properties.PropertySpecService propertySpecService, NlsService nlsService, X509Service x509Service, KeyStoreService keyStoreService) {
        super(thesaurus, propertySpecService);
        this.nlsService = nlsService;
        this.x509Service = x509Service;
        this.keyStoreService = keyStoreService;
    }

    @Override
    public ConnectionType getConnectionTypeSupport() {
        return new com.energyict.mdc.channels.ip.socket.TLSConnectionType(propertySpecService, nlsService, x509Service, keyStoreService);
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.OUTBOUND_TLS_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public PersistenceSupport<ConnectionProvider, OutboundIpConnectionProperties> getPersistenceSupport() {
        return new OutboundIpConnectionPropertiesPersistenceSupport();
    }
}