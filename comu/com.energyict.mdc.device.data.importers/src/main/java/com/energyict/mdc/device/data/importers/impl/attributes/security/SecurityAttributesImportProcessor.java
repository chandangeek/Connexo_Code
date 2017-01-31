/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.attributes.DynamicPropertyConverter;
import com.energyict.mdc.device.data.importers.impl.attributes.DynamicPropertyConverter.PropertiesConverterConfig;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.energyict.mdc.protocol.api.security.SecurityProperty;

import java.util.Optional;
import java.util.stream.Collectors;

public class SecurityAttributesImportProcessor extends AbstractDeviceDataFileImportProcessor<SecurityAttributesImportRecord> {

    private final PropertiesConverterConfig propertiesConverterConfig;

    private String securitySettingsName;

    SecurityAttributesImportProcessor(DeviceDataImporterContext context, SupportedNumberFormat numberFormat) {
        super(context);
        this.propertiesConverterConfig = PropertiesConverterConfig.newConfig().withNumberFormat(numberFormat);
    }

    @Override
    public void process(SecurityAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        validateSecuritySettingsNameUniquenessInFile(data);
        SecurityPropertySet deviceConfigSecurityPropertySet = device.getDeviceConfiguration().getSecurityPropertySets().stream()
                .filter(securityPropertySet -> securityPropertySet.getName().equals(data.getSecuritySettingsName())).findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SECURITY_SETTINGS_ON_DEVICE, data.getLineNumber(), data.getSecuritySettingsName()));
        TypedProperties typedProperties = getUpdatedProperties(device, deviceConfigSecurityPropertySet, data);
        try {
            device.setSecurityProperties(deviceConfigSecurityPropertySet, typedProperties);
            device.save();
        } catch (Exception e) {
            throw new ProcessorException(MessageSeeds.SECURITY_ATTRIBUTES_NOT_SET, data.getLineNumber(), data.getDeviceIdentifier());
        }
        logMissingPropertiesIfIncomplete(data, logger, device, deviceConfigSecurityPropertySet, typedProperties);
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private void logMissingPropertiesIfIncomplete(SecurityAttributesImportRecord data, FileImportLogger logger, Device device, SecurityPropertySet deviceConfigSecurityPropertySet, TypedProperties typedProperties) {
        if (device.getSecurityProperties(deviceConfigSecurityPropertySet).stream().anyMatch(securityProperty -> !securityProperty.isComplete())) {
            String missingProperties = deviceConfigSecurityPropertySet.getPropertySpecs().stream()
                    .filter(PropertySpec::isRequired)
                    .map(PropertySpec::getName)
                    .filter(propertySpec -> !typedProperties.hasValueFor(propertySpec))
                    .collect(Collectors.joining(", "));
            if (!missingProperties.isEmpty()) {
                logger.warning(MessageSeeds.REQUIRED_SECURITY_ATTRIBUTES_MISSED, data.getLineNumber(), missingProperties);
            }
        }
    }

    private TypedProperties getUpdatedProperties(Device device, SecurityPropertySet deviceConfigSecurityPropertySet, SecurityAttributesImportRecord data) {
        TypedProperties typedProperties = getTypedPropertiesForSecurityPropertySet(device, deviceConfigSecurityPropertySet);
        for (PropertySpec propertySpec : deviceConfigSecurityPropertySet.getPropertySpecs()) {
            if (data.getSecurityAttributes().containsKey(propertySpec.getName())) {
                Object newPropertyValue = parseStringToValue(propertySpec, data.getSecurityAttributes().get(propertySpec.getName()), data);
                typedProperties.setProperty(propertySpec.getName(), newPropertyValue);
            } else {
                typedProperties.removeProperty(propertySpec.getName());
            }
        }
        return typedProperties;
    }

    private TypedProperties getTypedPropertiesForSecurityPropertySet(Device device, SecurityPropertySet securityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (SecurityProperty securityProperty : device.getSecurityProperties(securityPropertySet)) {
            typedProperties.setProperty(securityProperty.getName(), securityProperty.getValue());
        }
        return typedProperties;
    }

    private Object parseStringToValue(PropertySpec propertySpec, String value, SecurityAttributesImportRecord data) {
        ValueFactory<?> valueFactory = propertySpec.getValueFactory();
        Optional<DynamicPropertyConverter> propertyParser = DynamicPropertyConverter.of(valueFactory.getClass());
        Object parsedValue;
        try {
            if (propertyParser.isPresent()) {
                value = propertyParser.get().configure(propertiesConverterConfig).convert(value);
            }
            parsedValue = valueFactory.fromStringValue(value);
        } catch (Exception e) {
            String expectedFormat = propertyParser.isPresent() ? propertyParser.get().getExpectedFormat(getContext().getThesaurus()) : valueFactory.getValueType().getName();
            throw new ProcessorException(MessageSeeds.LINE_FORMAT_ERROR, data.getLineNumber(), propertySpec.getName(), expectedFormat);
        }
        try {
            propertySpec.validateValue(parsedValue);
        } catch (InvalidValueException e) {
            throw new ProcessorException(MessageSeeds.SECURITY_ATTRIBUTE_INVALID_VALUE, data.getLineNumber(), parsedValue, propertySpec.getName());
        }
        return parsedValue;
    }

    private void validateSecuritySettingsNameUniquenessInFile(SecurityAttributesImportRecord data) {
        if (securitySettingsName == null) {
            securitySettingsName = data.getSecuritySettingsName();
        } else if (!securitySettingsName.equals(data.getSecuritySettingsName())) {
            throw new ProcessorException(MessageSeeds.SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE, data.getLineNumber()).andStopImport();
        }
    }
}
