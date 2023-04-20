/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.attributes.security;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.attributes.PropertySpecAwareConstraintViolationException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports the values for security properties (keys and passwords), only supports plaintext keys and passwords.
 * PropertySpecs for the SecurityPropertySet yield KeyAccessorTypes.
 * The device will have KeyAccessors for the KeyAccessorTypes (or the importer creates them)
 * The real value for the property will be stored on the actual-value of the KeyAccessor
 */
public class SecurityAttributesImportProcessor extends AbstractDeviceDataFileImportProcessor<SecurityAttributesImportRecord> {

    private String securitySettingsName;
    private final SecurityManagementService securityManagementService;

    SecurityAttributesImportProcessor(DeviceDataImporterContext context, SecurityManagementService securityManagementService) {
        super(context);
        this.securityManagementService = securityManagementService;
    }

    @Override
    public void process(SecurityAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        validateSecuritySettingsNameUniquenessInFile(data);
        SecurityPropertySet deviceConfigSecurityPropertySet = device.getDeviceConfiguration().getSecurityPropertySets().stream()
                .filter(securityPropertySet -> securityPropertySet.getName().equals(data.getSecuritySettingsName()))
                .findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SECURITY_SETTINGS_ON_DEVICE, data.getLineNumber(), data.getSecuritySettingsName()));
        try {
            updatedProperties(device, deviceConfigSecurityPropertySet, data);
        } catch (Exception e) {
            logger.importLineFailed(data.getLineNumber(), e);
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
            try {
                if (data.getSecurityAttributes().containsKey(propertySpec.getName())) {
                    ConfigurationSecurityProperty securityProperty = securityProperties.stream()
                            .filter(sp -> sp.getName().equals(propertySpec.getName()))
                            .findAny()
                            .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_VALUE_FOR_SECURITY_PROPERTY, data.getLineNumber(), propertySpec
                                    .getName()));

                    SecurityAccessorType securityAccessorType = securityProperty.getSecurityAccessorType();
                    securityManagementService.getDeviceSecretImporter(securityAccessorType); // to verify that security accessor type is supported by import
                    // TODO: try refactoring further code with DeviceSecretImporter
                    SecurityAccessor<SecurityValueWrapper> securityAccessor = device.getSecurityAccessor(securityAccessorType)
                            .orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
                    if (!securityAccessor.getActualValue().isPresent()) {
                        createNewActualValue(securityAccessor, securityAccessorType);
                    }
                    setPropertyOnSecurityAccessor(data, propertySpec, securityAccessor.getActualValue().get());
                }
            } catch (ConstraintViolationException e) {
                throw new PropertySpecAwareConstraintViolationException(propertySpec, e);
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

    private void createNewActualValue(SecurityAccessor<SecurityValueWrapper> securityAccessor, SecurityAccessorType securityAccessorType) {
        SecurityValueWrapper newValue;
        switch (securityAccessorType.getKeyType().getCryptographicType()) {
            case SymmetricKey:
            case Hsm:
                newValue = securityManagementService.newSymmetricKeyWrapper(securityAccessorType);
                break;
            case Passphrase:
                newValue = securityManagementService.newPassphraseWrapper(securityAccessorType);
                break;
            default:
                throw new IllegalStateException("Import of values of this security accessor is not supported: "+ securityAccessorType
                        .getName());
        }
        securityAccessor.setActualValue(newValue);
        securityAccessor.save();
    }

    private void validateSecuritySettingsNameUniquenessInFile(SecurityAttributesImportRecord data) {
        if (securitySettingsName == null) {
            securitySettingsName = data.getSecuritySettingsName();
        } else if (!securitySettingsName.equals(data.getSecuritySettingsName())) {
            throw new ProcessorException(MessageSeeds.SECURITY_SETTINGS_NAME_IS_NOT_UNIQUE_IN_FILE, data.getLineNumber()).andStopImport();
        }
    }
}
