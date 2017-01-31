/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.ip.socket;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionProperties;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionPropertiesPersistenceSupport;
import com.energyict.protocols.impl.channels.ip.OutboundIpCustomPropertySet;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link OutboundTcpIpConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (12:03)
 */
public class OutboundTcpIpCustomPropertySet extends OutboundIpCustomPropertySet {

    public OutboundTcpIpCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this(thesaurus, CustomPropertySetTranslationKeys.OUTBOUND_TCP_CUSTOM_PROPERTY_SET_NAME, propertySpecService);
    }

    protected OutboundTcpIpCustomPropertySet(Thesaurus thesaurus, TranslationKey translationKey, PropertySpecService propertySpecService) {
        super(thesaurus, translationKey, propertySpecService);
    }

    @Override
    public PersistenceSupport<ConnectionProvider, OutboundIpConnectionProperties> getPersistenceSupport() {
        return new OutboundIpConnectionPropertiesPersistenceSupport();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.hostPropertySpec(),
                this.portPropertySpec(),
                this.connectionTimeoutPropertySpec());
    }

}