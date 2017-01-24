package com.energyict.protocolimplv2.ace4000;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.tasks.ACE4000DeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link ACE4000DeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (13:15)
 */
public class ACE4000DeviceProtocolDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, ACE4000DeviceProtocolDialectProperties> {

    private volatile PropertySpecService propertySpecService;

    @Inject
    public ACE4000DeviceProtocolDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, ACE4000DeviceProtocolDialectProperties> getPersistenceSupport() {
        return new ACE4000DeviceProtocolDialectPropertyPersistenceSupport();
    }

    @Override
    protected DeviceProtocolDialect getDeviceProtocolDialect() {
        return new ACE4000DeviceProtocolDialect(propertySpecService);
    }
}