package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import java.util.Optional;

/**
 * A dialect related to LoadProfiles.
 *
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:46
 */
public class SDKLoadProfileProtocolDialect extends AbstractDeviceProtocolDialect {

    private final PropertySpecService propertySpecService;

    public SDKLoadProfileProtocolDialect(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return "SDKLoadProfileDialect";
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for loadProfile testing";
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new SDKLoadProfileDialectCustomPropertySet(this.propertySpecService));
    }

}