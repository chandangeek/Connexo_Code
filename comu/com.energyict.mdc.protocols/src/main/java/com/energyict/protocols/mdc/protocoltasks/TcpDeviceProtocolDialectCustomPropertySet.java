package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.common.AbstractDialectCustomPropertySet;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link TcpDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (14:44)
 */
public class TcpDeviceProtocolDialectCustomPropertySet extends AbstractDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, TcpDeviceProtocolDialectProperties> {

    private volatile PropertySpecService propertySpecService;

    @Inject
    public TcpDeviceProtocolDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, TcpDeviceProtocolDialectProperties> getPersistenceSupport() {
        return new TcpDeviceProtocolDialectPropertyPersistenceSupport();
    }

    @Override
    protected DeviceProtocolDialect getDeviceProtocolDialect() {
        return new TcpDeviceProtocolDialect(propertySpecService);
    }
}