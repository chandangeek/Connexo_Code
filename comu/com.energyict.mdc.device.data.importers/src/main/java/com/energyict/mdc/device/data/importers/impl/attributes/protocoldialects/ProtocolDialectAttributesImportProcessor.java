/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.protocoldialects;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.attributes.DynamicPropertyConverter;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import java.util.Optional;

public class ProtocolDialectAttributesImportProcessor extends AbstractDeviceDataFileImportProcessor<ProtocolDialectAttributesImportRecord> {

    private final DynamicPropertyConverter.PropertiesConverterConfig propertiesConverterConfig;

    ProtocolDialectAttributesImportProcessor(DeviceDataImporterContext context, SupportedNumberFormat numberFormat) {
        super(context);
        this.propertiesConverterConfig = DynamicPropertyConverter.PropertiesConverterConfig.newConfig().withNumberFormat(numberFormat);
    }

    @Override
    public void process(ProtocolDialectAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        ProtocolDialectConfigurationProperties protocolDialect = getProtocolDialect(device, data);
                data.getAttributes().entrySet().stream()
                .filter(attribute -> !Checks.is(attribute.getValue()).emptyOrOnlyWhiteSpace())
                .forEach(attribute -> setProtocolDialectProperty(device, attribute.getKey(), attribute.getValue(), protocolDialect, data, logger));
        device.save();
    }

    private void setProtocolDialectProperty(Device device, String name, String value, ProtocolDialectConfigurationProperties protocolDialect, ProtocolDialectAttributesImportRecord data, FileImportLogger logger){
        Optional<PropertySpec> propertySpec = getPropertySpec(protocolDialect, name);
        if (!propertySpec.isPresent()){
            logger.warning(MessageSeeds.UNKNOWN_PROTOCOL_DIALECT_ATTRIBUTE, protocolDialect.getDeviceProtocolDialectName(), name);
            device.setProtocolDialectProperty(protocolDialect.getDeviceProtocolDialectName(), name, value);
        } else {
            device.setProtocolDialectProperty(protocolDialect.getDeviceProtocolDialectName(), propertySpec.get().getName(), parseStringToValue(propertySpec.get(), value, data));
        }
    }

    private  ProtocolDialectConfigurationProperties getProtocolDialect(Device device, ProtocolDialectAttributesImportRecord data) {
        return device.getProtocolDialects().stream()
                .filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getDeviceProtocolDialectName().equals(data.getProtocolDialect()))
                .findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE_PROTOCOL_DIALECT_ON_DEVICE, data.getLineNumber(), data.getProtocolDialect()));
    }

    private Optional<PropertySpec> getPropertySpec(ProtocolDialectConfigurationProperties protocolDialect, String name) {
        return protocolDialect.getPropertySpecs().stream()
                .filter(propertySpec -> name.equalsIgnoreCase(propertySpec.getName())).findFirst();
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private Object parseStringToValue(PropertySpec propertySpec, String value, ProtocolDialectAttributesImportRecord data) {
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
            throw new ProcessorException(MessageSeeds.PROTOCOL_DIALECT_ATTRIBUTE_INVALID_VALUE, data.getLineNumber(), value, propertySpec.getName());
        }
        return parsedValue;
    }
}
