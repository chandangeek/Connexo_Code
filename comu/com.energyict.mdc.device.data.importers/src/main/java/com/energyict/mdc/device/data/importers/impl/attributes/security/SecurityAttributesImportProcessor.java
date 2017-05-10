/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports the actual values for KeyAccessors defined through SecurityAccessorTypes on DeviceConfig.
 */
public class SecurityAttributesImportProcessor extends AbstractDeviceDataFileImportProcessor<SecurityAttributesImportRecord> { //TODO

    private String securitySettingsName;
    private final PkiService pkiService;

    SecurityAttributesImportProcessor(DeviceDataImporterContext context, PkiService pkiService) {
        super(context);
        this.pkiService = pkiService;
    }

    @Override
    public void process(SecurityAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        validateSecuritySettingsNameUniquenessInFile(data);
        SecurityPropertySet deviceConfigSecurityPropertySet = device.getDeviceConfiguration().getSecurityPropertySets().stream()
                .filter(securityPropertySet -> securityPropertySet.getName().equals(data.getSecuritySettingsName())).findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SECURITY_SETTINGS_ON_DEVICE, data.getLineNumber(), data.getSecuritySettingsName()));
        try {
            updatedProperties(device, deviceConfigSecurityPropertySet, data);
        } catch (Exception e) {
            throw new ProcessorException(MessageSeeds.SECURITY_ATTRIBUTES_NOT_SET, data.getLineNumber(), data.getDeviceIdentifier());
        }
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private void updatedProperties(Device device, SecurityPropertySet deviceConfigSecurityPropertySet, SecurityAttributesImportRecord data) {
        List<ConfigurationSecurityProperty> securityProperties = deviceConfigSecurityPropertySet.getConfigurationSecurityProperties();
        for (PropertySpec propertySpec : deviceConfigSecurityPropertySet.getPropertySpecs()) {
            if (data.getSecurityAttributes().containsKey(propertySpec.getName())) {
                ConfigurationSecurityProperty securityProperty = securityProperties.stream()
                        .filter(sp -> sp.getName().equals(propertySpec.getName()))
                        .findAny()
                        .orElseThrow(()->new ProcessorException(MessageSeeds.NO_VALUE_FOR_SECURITY_PROPERTY, data.getLineNumber(), propertySpec.getName()));

                KeyAccessorType keyAccessorType = securityProperty.getKeyAccessorType();
                KeyAccessor<SecurityValueWrapper> keyAccessor = device.getKeyAccessor(keyAccessorType)
                        .orElseGet(() -> device.newKeyAccessor(keyAccessorType));
                if (!keyAccessor.getActualValue().isPresent()) {
                    createNewActualValue(keyAccessor, keyAccessorType);
                }
                setPropertyOnSecurityAccessor(data, propertySpec, keyAccessor.getActualValue().get());
            }
        }
    }

    private void setPropertyOnSecurityAccessor(SecurityAttributesImportRecord data, PropertySpec propertySpec, SecurityValueWrapper actualValue) {
        Map<String, Object> properties = new HashMap<>();
        String value = data.getSecurityAttributes().get(propertySpec.getName());
        if (actualValue instanceof PlaintextSymmetricKey) {
            properties.put("key", value);
        } else if (actualValue instanceof PlaintextPassphrase) {
            properties.put("passphrase", value);
        } else {
            throw new ProcessorException(MessageSeeds.UNKNOWN_KEY_WRAPPER, data.getLineNumber(), data.getDeviceIdentifier());
        }
        actualValue.setProperties(properties);
    }

    private void createNewActualValue(KeyAccessor<SecurityValueWrapper> keyAccessor, KeyAccessorType keyAccessorType) {
        SecurityValueWrapper newValue;
        switch (keyAccessorType.getKeyType().getCryptographicType()) {
            case SymmetricKey:
                newValue = pkiService.newSymmetricKeyWrapper(keyAccessorType);
                break;
            case Passphrase:
                newValue = pkiService.newPassphraseWrapper(keyAccessorType);
                break;
            default:
                throw new IllegalStateException("Import of values of this security accessor is not supported: "+keyAccessorType.getName());
        }
        keyAccessor.setActualValue(newValue);
        keyAccessor.save();
    }

    private void validateSecuritySettingsNameUniquenessInFile(SecurityAttributesImportRecord data) {
        if (securitySettingsName == null) {
            securitySettingsName = data.getSecuritySettingsName();
        } else if (!securitySettingsName.equals(data.getSecuritySettingsName())) {
            throw new ProcessorException(MessageSeeds.SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE, data.getLineNumber()).andStopImport();
        }
    }
}
