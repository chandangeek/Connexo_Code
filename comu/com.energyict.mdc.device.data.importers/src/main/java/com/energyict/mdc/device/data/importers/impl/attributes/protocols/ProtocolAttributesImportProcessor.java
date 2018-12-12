/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.protocols;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.attributes.DynamicPropertyConverter;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;

import java.util.Optional;

public class ProtocolAttributesImportProcessor extends AbstractDeviceDataFileImportProcessor<ProtocolAttributesImportRecord> {

    private final DynamicPropertyConverter.PropertiesConverterConfig propertiesConverterConfig;

    ProtocolAttributesImportProcessor(DeviceDataImporterContext context, SupportedNumberFormat numberFormat) {
        super(context);
        this.propertiesConverterConfig = DynamicPropertyConverter.PropertiesConverterConfig.newConfig().withNumberFormat(numberFormat);
    }

    @Override
    public void process(ProtocolAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        data.getAttributes().entrySet().stream()
                .filter(attribute -> !Checks.is(attribute.getValue()).emptyOrOnlyWhiteSpace())
                .forEach(attribute -> setProtocolProperty(device, attribute.getKey(), attribute.getValue(), data, logger));
        device.save();
    }

    private void setProtocolProperty(Device device, String name, String value, ProtocolAttributesImportRecord data, FileImportLogger logger ){
        Optional<PropertySpec> propertySpec = getPropertySpec(device, name);
        if (!propertySpec.isPresent()){
            logger.warning(MessageSeeds.UNKNOWN_PROTOCOL_ATTRIBUTE, data.getDeviceIdentifier(), name);
            device.setProtocolProperty(name, value);
        } else {
            device.setProtocolProperty(propertySpec.get().getName(), parseStringToValue(device, propertySpec.get(), value, data));
        }
    }

    private Optional<PropertySpec> getPropertySpec(Device device, String name) {
        return device.getDeviceType().getDeviceProtocolPluggableClass()
                .flatMap(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs().stream()
                        .filter(propertySpec -> name.equalsIgnoreCase(propertySpec.getName())).findFirst());
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private Object parseStringToValue(Device device, PropertySpec propertySpec, String value, ProtocolAttributesImportRecord data) {
        ValueFactory<?> valueFactory = propertySpec.getValueFactory();
        Optional<DynamicPropertyConverter> propertyParser = DynamicPropertyConverter.of(valueFactory.getClass());
        Object parsedValue;
        try {
            String convertedValue = value;
            if (propertyParser.isPresent()) {
                convertedValue = propertyParser.get().configure(propertiesConverterConfig).convert(value);
            } else if (SecurityAccessorType.class.isAssignableFrom(valueFactory.getValueType())){
                convertedValue = device.getDeviceType().getSecurityAccessorTypes().stream()
                        .filter(keyAccessorType -> keyAccessorType.getName().equalsIgnoreCase(value))
                        .map(keyAccessorType -> String.valueOf(keyAccessorType.getId()))
                        .findFirst()
                        .orElse(value);
            }
            parsedValue = valueFactory.fromStringValue(convertedValue);
        } catch (Exception e) {
            String expectedFormat = propertyParser.isPresent() ? propertyParser.get().getExpectedFormat(getContext().getThesaurus()) : valueFactory.getValueType().getName();
            throw new ProcessorException(MessageSeeds.LINE_FORMAT_ERROR, data.getLineNumber(), propertySpec.getName(), expectedFormat);
        }
        try {
            propertySpec.validateValue(parsedValue);
        } catch (InvalidValueException e) {
            throw new ProcessorException(MessageSeeds.ATTRIBUTE_INVALID_VALUE, data.getLineNumber(), value, propertySpec.getName());
        }
        return parsedValue;
    }
}
