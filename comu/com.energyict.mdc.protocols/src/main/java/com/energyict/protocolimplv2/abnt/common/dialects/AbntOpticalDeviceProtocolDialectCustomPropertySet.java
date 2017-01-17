package com.energyict.protocolimplv2.abnt.common.dialects;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link AbntOpticalDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (14:44)
 */
public class AbntOpticalDeviceProtocolDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, AbntDeviceProtocolDialectProperties> {

    private volatile PropertySpecService propertySpecService;

    @Inject
    public AbntOpticalDeviceProtocolDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, AbntDeviceProtocolDialectProperties> getPersistenceSupport() {
        return new AbntDeviceProtocolDialectPropertyPersistenceSupport();
    }

    @Override
    protected DeviceProtocolDialect getDeviceProtocolDialect() {
        return new AbntOpticalDeviceProtocolDialect(propertySpecService);
    }
}