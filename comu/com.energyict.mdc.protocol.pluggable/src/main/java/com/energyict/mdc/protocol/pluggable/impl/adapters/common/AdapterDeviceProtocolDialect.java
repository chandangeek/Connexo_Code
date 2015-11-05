package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an Adapter implementation fo the DeviceProtocolDialect.
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 13:37
 */
public class AdapterDeviceProtocolDialect implements DeviceProtocolDialect {

    private static final String REMOVABLE_SECURITY_LEVEL_PROPERTY = "SecurityLevel";

    private final ProtocolPluggableService protocolPluggableService;
    private final HasDynamicProperties withDynamicProperties;
    private final Set<String> removablePropertyNames;

    /**
     * Default constructor for the AdapterDeviceProtocolDialect
     *
     * @param withDynamicProperties The HasDynamicProperties
     * @param removablePropertySpecs  propertySpecs which should be removed (eg. SecurityPropertySpecs)
     */
    public AdapterDeviceProtocolDialect(ProtocolPluggableService protocolPluggableService, HasDynamicProperties withDynamicProperties, List<PropertySpec> removablePropertySpecs) {
        this.protocolPluggableService = protocolPluggableService;
        this.withDynamicProperties = withDynamicProperties;
        this.removablePropertyNames = new HashSet<>();
        for (PropertySpec removablePropertyName : removablePropertySpecs) {
            this.removablePropertyNames.add(removablePropertyName.getName());
        }
        this.removablePropertyNames.add(REMOVABLE_SECURITY_LEVEL_PROPERTY);
    }

    public AdapterDeviceProtocolDialect(PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, SmartMeterProtocol meterProtocol, List<PropertySpec> removablePropertySpecs) {
        this(protocolPluggableService, wrapAsDynamicProperties(propertySpecService, meterProtocol), removablePropertySpecs);
    }

    public AdapterDeviceProtocolDialect (PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, MeterProtocol meterProtocol, List<PropertySpec> removablePropertySpecs) {
        this(protocolPluggableService, wrapAsDynamicProperties(propertySpecService, meterProtocol), removablePropertySpecs);
    }

    private static HasDynamicProperties wrapAsDynamicProperties(PropertySpecService propertySpecService, ConfigurationSupport configurationSupport) {
        return new ConfigurationSupportToDynamicPropertiesAdapter(propertySpecService, configurationSupport);
    }

    @Override
    public String getDisplayName() {
        return "Default";
    }

    @Override
    public String getDeviceProtocolDialectName() {
        /*
        This should return the SimpleName of the legacy protocols, concatenated with its javaclassname hashcode.
        This name is used for creating a RelationType for this Dialect and his DeviceProtocol
        We add the hashCode because we have multiple protocols with the same name (like 'MbusDevice' ...)
         */
        if (this.withDynamicProperties instanceof ConfigurationSupportToDynamicPropertiesAdapter) {
            ConfigurationSupportToDynamicPropertiesAdapter adapter = (ConfigurationSupportToDynamicPropertiesAdapter) this.withDynamicProperties;
            ConfigurationSupport configurationSupport = adapter.getConfigurationSupport();
            return this.protocolPluggableService.createOriginalAndConformRelationNameBasedOnJavaClassname(configurationSupport.getClass());
        }
        else {
            return this.protocolPluggableService.createOriginalAndConformRelationNameBasedOnJavaClassname(this.withDynamicProperties.getClass());
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        List<PropertySpec> propertySpecs =
            this.withDynamicProperties.getPropertySpecs()
                .stream()
                .filter(propertySpec -> !this.removablePropertyNames.contains(propertySpec.getName()))
                .collect(Collectors.toList());
        return removeDuplicates(propertySpecs);
    }

    private List<PropertySpec> removeDuplicates(List<PropertySpec> original) {
        List<PropertySpec> nonDuplicateSpecs = new ArrayList<>();
        for (PropertySpec propertySpec : original) {
            if (!nonDuplicateSpecs.contains(propertySpec)) {
                nonDuplicateSpecs.add(propertySpec);
            }
        }
        return nonDuplicateSpecs;
    }

}