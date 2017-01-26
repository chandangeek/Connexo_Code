package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;

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

    @Inject
    SDKBreakerDialectCustomPropertySet(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    protected DeviceProtocolDialect getDeviceProtocolDialect() {
        return new SDKBreakerTaskProtocolDialectProperties();
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKBreakerDialectProperties> getPersistenceSupport() {
        return new SDKBreakerDialectPropertyPersistenceSupport();
    }
}