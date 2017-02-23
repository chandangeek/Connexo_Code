package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.TranslationKeys;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an Adapter implementation for the {@link DeviceProtocolDialect} interface
 * to support the legacy protocols.
 * <p>
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

    public AdapterDeviceProtocolDialect(Thesaurus thesaurus, SmartMeterProtocol meterProtocol) {
        this(thesaurus, wrapAsDynamicProperties(meterProtocol));
    }

    public AdapterDeviceProtocolDialect(Thesaurus thesaurus, MeterProtocol meterProtocol) {
        this(thesaurus, wrapAsDynamicProperties(meterProtocol));
    }

    private static HasDynamicProperties wrapAsDynamicProperties(ConfigurationSupport configurationSupport) {
        return new ConfigurationSupportToDynamicPropertiesAdapter(configurationSupport);
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.LEGACY_PROTOCOL).format();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::new).collect(Collectors.toList());
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
        } else {
            return this.withDynamicProperties.getClass().getName();
        }
    }
}