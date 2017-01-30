package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import test.com.energyict.protocolimplv2.sdksample.SDKLoadProfileProtocolDialectProperties;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SDKLoadProfileProtocolDialectProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
public class SDKLoadProfileDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, SDKLoadProfileDialectProperties> {

    private final PropertySpecService propertySpecService;

    @Inject
    public SDKLoadProfileDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    @Override
    protected DeviceProtocolDialect getDeviceProtocolDialect() {
        return new SDKLoadProfileProtocolDialectProperties(this.propertySpecService);
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKLoadProfileDialectProperties> getPersistenceSupport() {
        return new SDKLoadProfileDialectPropertyPersistenceSupport();
    }
}