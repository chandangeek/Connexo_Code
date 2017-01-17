package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import test.com.energyict.protocolimplv2.sdksample.SDKFirmwareTaskProtocolDialectProperties;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SDKFirmwareTaskProtocolDialectProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
class SDKFirmwareDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, SDKFirmwareDialectProperties> {

    @Inject
    public SDKFirmwareDialectCustomPropertySet(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKFirmwareDialectProperties> getPersistenceSupport() {
        return new SDKFirmwareDialectPropertyPersistenceSupport();
    }

    @Override
    protected DeviceProtocolDialect getDeviceProtocolDialect() {
        return new SDKFirmwareTaskProtocolDialectProperties();
    }
}