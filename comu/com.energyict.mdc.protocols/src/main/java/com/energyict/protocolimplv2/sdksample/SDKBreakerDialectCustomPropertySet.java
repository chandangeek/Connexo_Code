package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import test.com.energyict.protocolimplv2.sdksample.SDKBreakerTaskProtocolDialectProperties;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SDKBreakerTaskProtocolDialectProperties}.
 *
 * @author sva
 * @since 8/04/2016 - 13:10
 */
class SDKBreakerDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, SDKBreakerDialectProperties> {

    private final PropertySpecService propertySpecService;

    @Inject
    SDKBreakerDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    @Override
    protected DeviceProtocolDialect getDeviceProtocolDialect() {
        return new SDKBreakerTaskProtocolDialectProperties(this.propertySpecService);
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKBreakerDialectProperties> getPersistenceSupport() {
        return new SDKBreakerDialectPropertyPersistenceSupport();
    }
}