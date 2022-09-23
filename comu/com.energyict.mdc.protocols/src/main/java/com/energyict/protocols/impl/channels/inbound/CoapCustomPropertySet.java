/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.inbound.CoapConnectionType;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.impl.channels.AbstractConnectionTypeCustomPropertySet;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link CoapConnectionType} class hierarcy.
 */
public class CoapCustomPropertySet extends AbstractConnectionTypeCustomPropertySet implements CustomPropertySet<ConnectionProvider, CoapConnectionProperties> {

    @Inject
    public CoapCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.COAP_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public PersistenceSupport<ConnectionProvider, CoapConnectionProperties> getPersistenceSupport() {
        return new CoapConnectionPropertiesPersistenceSupport();
    }

    @Override
    public ConnectionType getConnectionTypeSupport() {
        return new CoapConnectionType(propertySpecService);
    }
}