/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.us.protocolimplv2.mercury;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.elster.us.protocolimplv2.mercury.minimax.MiniMaxTcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link MiniMaxTcpDeviceProtocolDialect}.
 *
 * @author Stijn Vanhoorelbeke
 * @since 12.07.17 - 11:42
 */
public class MiniMaxTcpDeviceProtocolDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, MiniMaxTcpDeviceProtocolDialectProperties> {

    @Inject
    public MiniMaxTcpDeviceProtocolDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, MiniMaxTcpDeviceProtocolDialectProperties> getPersistenceSupport() {
        return new MiniMaxTcpDeviceProtocolDialectPropertyPersistenceSupport();
    }

    @Override
    public DeviceProtocolDialect getDeviceProtocolDialect() {
        return new MiniMaxTcpDeviceProtocolDialect(propertySpecService, getUplThesaurus());
    }
}
