/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.hsm.HsmConfigurationService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmConfiguration;
import com.elster.jupiter.hsm.model.configuration.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.keys.AesDeviceKey;
import com.elster.jupiter.hsm.model.keys.DeviceKey;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.KeyType;
import com.elster.jupiter.hsm.model.keys.TransportKey;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;
import com.elster.jupiter.pki.HsmSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedEncryptedDataType;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.InvalidAlgorithm;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class SecureHSMDeviceShipmentImporter extends SecureDeviceImporterAbstract implements FileImporter {

    private HsmEnergyService hsmEnergyService;
    private HsmConfigurationService hsmConfigurationService;
    private SecurityManagementService securityManagementService;

    public SecureHSMDeviceShipmentImporter(ImporterProperties importerProperties) {
        super(importerProperties.getThesaurus(), importerProperties.getTrustStore(), importerProperties.getDeviceConfigurationService(), importerProperties.getDeviceService(), importerProperties.getSecurityManagementService(), importerProperties
                .getImportExtension());
        hsmEnergyService = importerProperties.getHsmEnergyService();
        securityManagementService = importerProperties.getSecurityManagementService();
        hsmConfigurationService = importerProperties.getHsmConfigurationService();
    }

    @Override
    protected void importDeviceKey(Device device, NamedEncryptedDataType deviceKey, Map<String, WrapKey> wrapKeyMap, Logger logger) throws HsmBaseException {
        HsmConfiguration hsmConfiguration = hsmConfigurationService.getHsmConfiguration();
        /**
         * We need to keep 2 labels while the one from received file can be mapped to a different one in our HSM config
         */
        String fileImportLabel = deviceKey.getWrapKeyLabel();
        String importLabel = hsmConfiguration.map(fileImportLabel);

        AsymmetricAlgorithm asymmetricAlgorithm = getAvailableAsymmetricAlgorithm(wrapKeyMap, fileImportLabel);
        SymmetricAlgorithm symmetricAlgorithm = getAvailableSymmetricAlgorithm(deviceKey);

        TransportKey tkey = new TransportKey(importLabel, asymmetricAlgorithm,  wrapKeyMap.get(fileImportLabel).getSymmetricKey().getCipherData().getCipherValue(), symmetricAlgorithm.getKeySize());

        ImportFileDeviceKey importFileDeviceKey = new ImportFileDeviceKey(deviceKey.getCipherData().getCipherValue());
        byte[] initVector = importFileDeviceKey.getInitializationVector();
        byte[] deviceKeyBytes = importFileDeviceKey.getCipher();

        HsmLabelConfiguration importLabelConfiguration = hsmConfiguration.get(importLabel);
        KeyType importKeyType = importLabelConfiguration.getImportKeyType();
        DeviceKey dkey = new AesDeviceKey(symmetricAlgorithm, importLabelConfiguration.getImportDeviceKeyLength() , deviceKeyBytes, initVector, importKeyType);
        HsmEncryptedKey hsmEncryptedKey = null;
        try {
            hsmEncryptedKey = hsmEnergyService.importKey(tkey, dkey, importLabelConfiguration.getImportReEncryptHsmLabel(), importKeyType);
        } catch (HsmBaseException e) {
            e.printStackTrace();
        }
        String securityAccessorName = deviceKey.getName();
        SecurityAccessorType securityAccessorType = getSecurityAccessorType(device, securityAccessorName, logger);
        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(securityAccessorType);
        if (securityAccessorOptional.flatMap(SecurityAccessor::getActualValue).isPresent()) {
            log(logger, MessageSeeds.ACTUAL_VALUE_ALREADY_EXISTS, securityAccessorName, device.getName());
        } else {
            SecurityAccessor securityAccessor = securityAccessorOptional.orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
            HsmSymmetricKey hsmSymmetricKey = (HsmSymmetricKey) securityManagementService.newSymmetricKeyWrapper(securityAccessorType);
            hsmSymmetricKey.setKey(hsmEncryptedKey.getEncryptedKey(), importLabelConfiguration.getImportReEncryptHsmLabel());
            securityAccessor.setActualValue(hsmSymmetricKey);
            securityAccessor.save();
        }
    }

    private AsymmetricAlgorithm getAvailableAsymmetricAlgorithm(Map<String, WrapKey> wrapKeyMap, String wrapKeyLabel) {
        String asymmetricAlgorithmName = super.getAsymmetricAlgorithm(wrapKeyMap.get(wrapKeyLabel));
        return Arrays.stream(AsymmetricAlgorithm.values())
                .filter(s -> s.getCipher().equals(asymmetricAlgorithmName))
                .findFirst()
                .orElseThrow(() -> new InvalidAlgorithm(asymmetricAlgorithmName));
    }

    private SymmetricAlgorithm getAvailableSymmetricAlgorithm(NamedEncryptedDataType deviceKey) {
        String symmetricAlgorithmName = super.getSymmetricAlgorithm(deviceKey);
        return Arrays.stream(SymmetricAlgorithm.values())
                .filter((algo -> algo.getCipher().equals(symmetricAlgorithmName)))
                .findFirst()
                .orElseThrow(() -> new InvalidAlgorithm(symmetricAlgorithmName));
    }

    @Override
    protected boolean shouldValidateCert() {
        return false;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        processFile(fileImportOccurrence);
    }
}
