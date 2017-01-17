package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.tasks.EiWebPlusDialect;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link EiWebPlusDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (08:59)
 */
public class EiWebPlusDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, EiWebPlusDialectProperties> {

    private volatile PropertySpecService propertySpecService;

    @Inject
    public EiWebPlusDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, EiWebPlusDialectProperties> getPersistenceSupport() {
        return new EiWebPlusDialectPropertyPersistenceSupport();
    }

    @Override
    protected DeviceProtocolDialect getDeviceProtocolDialect() {
        return new EiWebPlusDialect(propertySpecService);
    }
}