/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.ip.socket;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionProperties;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionPropertiesPersistenceSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link TcpIpPostDialConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:04)
 */
public class TcpIpPostDialCustomPropertySet extends OutboundTcpIpCustomPropertySet {

    public TcpIpPostDialCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, CustomPropertySetTranslationKeys.OUTBOUND_TCP_POST_DIAL_CUSTOM_PROPERTY_SET_NAME, propertySpecService);
    }

    @Override
    public PersistenceSupport<ConnectionProvider, OutboundIpConnectionProperties> getPersistenceSupport() {
        return new OutboundIpConnectionPropertiesPersistenceSupport();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.postDialDelayMillisPropertySpec());
        propertySpecs.add(this.postDialCommandAttempsPropertySpec());
        propertySpecs.add(this.postDialCommandPropertySpec());
        return propertySpecs;
    }

}