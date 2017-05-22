/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.protocols.impl.channels.ip.socket;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link com.energyict.mdc.channels.ip.socket.TLSConnectionType}.
 */
public class OutboundTlsCustomPropertySet extends OutboundTcpIpCustomPropertySet {

    private final CertificateWrapperExtractor certificateWrapperExtractor;

    @Inject
    public OutboundTlsCustomPropertySet(Thesaurus thesaurus, com.energyict.mdc.upl.properties.PropertySpecService propertySpecService, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(thesaurus, propertySpecService);
        this.certificateWrapperExtractor = certificateWrapperExtractor;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.OUTBOUND_TLS_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public ConnectionType getConnectionTypeSupport() {
        return new com.energyict.mdc.channels.ip.socket.TLSConnectionType(propertySpecService, certificateWrapperExtractor);
    }
}