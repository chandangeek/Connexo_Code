/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import test.com.energyict.protocolimplv2.sdksample.SDKCreditTaskProtocolDialectProperties;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SDKCreditTaskProtocolDialectProperties}.
 *
 * @author dborisov H403395 dmitriy.borisov@orioninc.com
 * @since 8/04/2021 - 13:10
 */
public class SDKCreditDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, SDKCreditDialectProperties> {

    @Inject
    public SDKCreditDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
    }

    @Override
    public DeviceProtocolDialect getDeviceProtocolDialect() {
        return new SDKCreditTaskProtocolDialectProperties(propertySpecService, getUplThesaurus());
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKCreditDialectProperties> getPersistenceSupport() {
        return new SDKCreditDialectPropertyPersistenceSupport();
    }
}