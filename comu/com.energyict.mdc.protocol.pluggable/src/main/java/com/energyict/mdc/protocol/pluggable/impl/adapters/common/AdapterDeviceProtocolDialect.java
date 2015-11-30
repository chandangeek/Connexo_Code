package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.TranslationKeys;

import java.util.Optional;

/**
 * Provides an Adapter implementation for the {@link DeviceProtocolDialect} interface
 * to support the legacy protocols.
 *
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 13:37
 */
public class AdapterDeviceProtocolDialect implements DeviceProtocolDialect {

    private final Thesaurus thesaurus;
    private final HasDynamicProperties withDynamicProperties;

    private AdapterDeviceProtocolDialect(Thesaurus thesaurus, HasDynamicProperties withDynamicProperties) {
        super();
        this.thesaurus = thesaurus;
        this.withDynamicProperties = withDynamicProperties;
    }

    public AdapterDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService, SmartMeterProtocol meterProtocol) {
        this(thesaurus, wrapAsDynamicProperties(propertySpecService, meterProtocol));
    }

    public AdapterDeviceProtocolDialect(Thesaurus thesaurus, PropertySpecService propertySpecService, MeterProtocol meterProtocol) {
        this(thesaurus, wrapAsDynamicProperties(propertySpecService, meterProtocol));
    }

    private static HasDynamicProperties wrapAsDynamicProperties(PropertySpecService propertySpecService, ConfigurationSupport configurationSupport) {
        return new ConfigurationSupportToDynamicPropertiesAdapter(configurationSupport);
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.LEGACY_PROTOCOL).format();
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.empty();
    }

    @Override
    public String getDeviceProtocolDialectName() {
        if (this.withDynamicProperties instanceof ConfigurationSupportToDynamicPropertiesAdapter) {
            ConfigurationSupportToDynamicPropertiesAdapter adapter = (ConfigurationSupportToDynamicPropertiesAdapter) this.withDynamicProperties;
            ConfigurationSupport configurationSupport = adapter.getConfigurationSupport();
            return configurationSupport.getClass().getName();
        }
        else {
            return this.withDynamicProperties.getClass().getName();
        }
    }

}