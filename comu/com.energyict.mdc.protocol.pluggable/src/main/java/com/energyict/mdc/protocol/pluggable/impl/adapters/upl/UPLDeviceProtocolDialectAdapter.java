package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.DialectCustomPropertySetNameDetective;
import com.energyict.mdc.protocol.pluggable.adapters.upl.cps.UnableToLoadCustomPropertySetClass;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/01/2017 - 16:33
 */
public class UPLDeviceProtocolDialectAdapter implements DeviceProtocolDialect {

    private static DialectCustomPropertySetNameDetective dialectCustomPropertySetNameDetective;

    private final com.energyict.mdc.upl.DeviceProtocolDialect uplDeviceProtocolDialect;
    private final CustomPropertySetInstantiatorService customPropertySetInstantiatorService;

    public UPLDeviceProtocolDialectAdapter(com.energyict.mdc.upl.DeviceProtocolDialect uplDeviceProtocolDialect, CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
        this.uplDeviceProtocolDialect = uplDeviceProtocolDialect;
        this.customPropertySetInstantiatorService = customPropertySetInstantiatorService;
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        this.ensureDialectCustomPropertySetNameMappingLoaded();
        String cpsJavaClassName = dialectCustomPropertySetNameDetective.customPropertySetClassNameFor(this.uplDeviceProtocolDialect.getClass());

        if (Checks.is(cpsJavaClassName).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(customPropertySetInstantiatorService.createCustomPropertySet(cpsJavaClassName));
            } catch (ClassNotFoundException e) {
                throw new UnableToLoadCustomPropertySetClass(e, cpsJavaClassName, DialectCustomPropertySetNameDetective.MAPPING_PROPERTIES_FILE_NAME);
            }
        }
    }

    private void ensureDialectCustomPropertySetNameMappingLoaded() {
        if (dialectCustomPropertySetNameDetective == null) {
            dialectCustomPropertySetNameDetective = new DialectCustomPropertySetNameDetective();
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return uplDeviceProtocolDialect.getUPLPropertySpecs();
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return uplDeviceProtocolDialect.getDeviceProtocolDialectName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return uplDeviceProtocolDialect.getDeviceProtocolDialectDisplayName();
    }
}