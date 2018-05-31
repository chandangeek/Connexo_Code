package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.model.keys.DeviceKey;
import com.elster.jupiter.hsm.model.keys.ImportedKey;
import com.elster.jupiter.hsm.model.keys.TransportKey;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.custom.energy.Energy;
import com.atos.worldline.jss.api.custom.energy.KeyImportResponse;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKey;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyCapability;
import com.atos.worldline.jss.api.key.KeyLabel;

public class HsmEnergyServiceImpl {

    /**
     *
     * @param tKey transport key to be used for import stage. This should be according to keylabel schema defined in HSM
     * @param dKey device key as extracted from import file
     * @param deviceKeyLabel key label to be used for re-encryption of the device key. This should be according to keylabel schema configured.
     * @param protectedSessionKey this should be configurable and decided based on keylabel schema. {@link ProtectedSessionKeyCapability}
     */
    public ImportedKey importKey(TransportKey tKey, DeviceKey dKey, String deviceKeyLabel, String protectedSessionKey) throws EncryptBaseException{
        try {
            ProtectedSessionKeyCapability capability = ProtectedSessionKeyCapability.valueOf(protectedSessionKey);
            KeyImportResponse keyImportResponse = Energy.keyImport(tKey.toHsmFormat(), tKey.getPaddingAlgorithm().toHsmFormat(), dKey.toHsmFormat(), new KeyLabel(deviceKeyLabel), capability);
            ProtectedSessionKey psk = keyImportResponse.getProtectedSessionKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new ImportedKey(psk.getValue(), kekLabel);
        } catch (EncryptBaseException|FunctionFailedException e) {
            throw new EncryptBaseException(e);
        }
    }

}
