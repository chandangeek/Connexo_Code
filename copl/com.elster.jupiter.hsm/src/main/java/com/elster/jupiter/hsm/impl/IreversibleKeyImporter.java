package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.custom.energy.Energy;
import com.atos.worldline.jss.api.custom.energy.KeyImportResponse;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKey;
import com.atos.worldline.jss.api.key.KeyLabel;

public class IreversibleKeyImporter  {

    public HsmEncryptedKey importKey(ImportKeyRequest importKeyRequest, HsmConfiguration hsmConfiguration) throws HsmBaseException {
        String encryptLabel = importKeyRequest.getStorageLabel();
        try {
            KeyImportResponse keyImportResponse = Energy.keyImport(importKeyRequest.getTransportKey(hsmConfiguration), importKeyRequest.getWrapperKeyAlgorithm()
                    .getHsmSpecs()
                    .getPaddingAlgorithm(), importKeyRequest.getDeviceKey(), new KeyLabel(encryptLabel), importKeyRequest.getHsmKeyType().getImportCapability()
                    .toProtectedSessionKeyCapability());
            ProtectedSessionKey psk = keyImportResponse.getProtectedSessionKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new HsmEncryptedKey(psk.getValue(), kekLabel);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

}
