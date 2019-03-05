/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import test.com.energyict.protocolimplv2.sdksample.SDKTopologyTaskProtocolDialectProperties;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SDKTopologyTaskProtocolDialectProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
public class SDKTopologyTaskDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, SDKTopologyTaskDialectProperties> {

    @Inject
    public SDKTopologyTaskDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService) {
        super(thesaurus, propertySpecService);
        customPropertySetService.addCustomPropertySet(this);
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKTopologyTaskDialectProperties> getPersistenceSupport() {
        return new SDKTopologyTaskDialectPropertyPersistenceSupport();
    }

    @Override
    public DeviceProtocolDialect getDeviceProtocolDialect() {
        return new SDKTopologyTaskProtocolDialectProperties(propertySpecService, getUplThesaurus());
    }
}