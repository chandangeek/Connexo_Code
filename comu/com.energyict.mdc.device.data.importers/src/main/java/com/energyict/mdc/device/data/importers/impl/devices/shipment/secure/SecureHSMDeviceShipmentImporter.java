/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment.secure;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.NamedEncryptedDataType;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.bindings.WrapKey;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.ImportFailedException;
import com.energyict.mdc.device.data.importers.impl.devices.shipment.secure.exception.InvalidAlgorithm;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class SecureHSMDeviceShipmentImporter extends SecureDeviceImporterAbstract implements FileImporter {

    private HsmEnergyService hsmEnergyService;
    private SecurityManagementService securityManagementService;

    public SecureHSMDeviceShipmentImporter(ImporterProperties importerProperties) {
        super(importerProperties.getThesaurus(), importerProperties.getTrustStore(), importerProperties.getDeviceConfigurationService(), importerProperties.getDeviceService(), importerProperties.getSecurityManagementService(), importerProperties
                .getImportExtension());
        hsmEnergyService = importerProperties.getHsmEnergyService();
        securityManagementService = importerProperties.getSecurityManagementService();
    }

    @Override
    protected void importDeviceKey(Device device, NamedEncryptedDataType deviceKey, Map<String, WrapKey> wrapKeyMap, Logger logger) throws HsmBaseException {
        String wrapperkeyLabel = deviceKey.getWrapKeyLabel();

        AsymmetricAlgorithm wrapperKeyAlgorithm = getAvailableAsymmetricAlgorithm(wrapKeyMap, wrapperkeyLabel);
        SymmetricAlgorithm symmetricAlgorithm = getAvailableSymmetricAlgorithm(deviceKey);

        ImportFileDeviceKey importFileDeviceKey = new ImportFileDeviceKey(deviceKey.getCipherData().getCipherValue());
        byte[] initVector = importFileDeviceKey.getInitializationVector();
        byte[] deviceKeyBytes = importFileDeviceKey.getCipher();

        String securityAccessorName = deviceKey.getName();
        SecurityAccessorType securityAccessorType = getSecurityAccessorType(device, securityAccessorName, logger).orElseThrow(() -> new ImportFailedException(MessageSeeds.NO_SUCH_KEY_ACCESSOR_TYPE_ON_DEVICE_TYPE, device.getName(), securityAccessorName));

        ImportKeyRequest ikr = new ImportKeyRequest(wrapperkeyLabel, wrapperKeyAlgorithm, wrapKeyMap.get(wrapperkeyLabel).getSymmetricKey().getCipherData().getCipherValue(), symmetricAlgorithm, deviceKeyBytes, initVector, securityAccessorType.getHsmKeyType());
        HsmEncryptedKey hsmEncryptedKey = hsmEnergyService.importKey(ikr);

        Optional<SecurityAccessor> securityAccessorOptional = device.getSecurityAccessor(securityAccessorType);
        if (securityAccessorOptional.flatMap(SecurityAccessor::getActualValue).isPresent()) {
            log(logger, MessageSeeds.ACTUAL_VALUE_ALREADY_EXISTS, securityAccessorName, device.getName());
        } else {
            SecurityAccessor securityAccessor = securityAccessorOptional.orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
            HsmKey hsmKey = (HsmKey) securityManagementService.newSymmetricKeyWrapper(securityAccessorType);
            hsmKey.setKey(hsmEncryptedKey.getEncryptedKey(), hsmEncryptedKey.getKeyLabel());
            securityAccessor.setActualValue(hsmKey);
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
        return true;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        processFile(fileImportOccurrence);
    }
}