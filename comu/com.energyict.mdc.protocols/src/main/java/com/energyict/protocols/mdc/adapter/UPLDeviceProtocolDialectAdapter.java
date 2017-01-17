package com.energyict.protocols.mdc.adapter;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.protocols.mdc.adapter.cps.DialectCustomPropertySetNameDetective;
import com.energyict.protocols.mdc.adapter.cps.UnableToCreateCustomPropertySet;
import com.energyict.protocols.mdc.adapter.cps.UnableToLoadCustomPropertySetClass;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

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

    private final Injector injector;
    private final com.energyict.mdc.upl.DeviceProtocolDialect uplDeviceProtocolDialect;

    public UPLDeviceProtocolDialectAdapter(com.energyict.mdc.upl.DeviceProtocolDialect uplDeviceProtocolDialect, Injector injector) {
        this.uplDeviceProtocolDialect = uplDeviceProtocolDialect;
        this.injector = injector;
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        this.ensureDialectCustomPropertySetNameMappingLoaded();
        return Optional
                .ofNullable(dialectCustomPropertySetNameDetective.customPropertySetClassNameFor(this.uplDeviceProtocolDialect.getClass()))
                .flatMap(this::loadClass)
                .map(this::toCustomPropertySet);
    }

    private void ensureDialectCustomPropertySetNameMappingLoaded() {
        if (dialectCustomPropertySetNameDetective == null) {
            dialectCustomPropertySetNameDetective = new DialectCustomPropertySetNameDetective();
        }
    }

    private Optional<Class> loadClass(String className) {
        if (Checks.is(className).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(this.getClass().getClassLoader().loadClass(className));
            } catch (ClassNotFoundException e) {
                throw new UnableToLoadCustomPropertySetClass(e, className, DialectCustomPropertySetNameDetective.MAPPING_PROPERTIES_FILE_NAME);
            }
        }
    }

    private CustomPropertySet toCustomPropertySet(Class cpsClass) {
        try {
            return (CustomPropertySet) this.injector.getInstance(cpsClass);
        } catch (ConfigurationException | ProvisionException e) {
            throw new UnableToCreateCustomPropertySet(e, cpsClass, DialectCustomPropertySetNameDetective.MAPPING_PROPERTIES_FILE_NAME);
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