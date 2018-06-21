package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.model.keys.DeviceKey;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.KeyType;
import com.elster.jupiter.hsm.model.keys.TransportKey;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.custom.energy.Energy;
import com.atos.worldline.jss.api.custom.energy.KeyImportResponse;
import com.atos.worldline.jss.api.custom.energy.KeyRenewalResponse;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKey;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyCapability;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyType;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.hsm.impl.HsmEnergyService", service = {HsmEnergyService.class}, immediate = true)
public class HsmEnergyServiceImpl implements HsmEnergyService {

    /**
     *
     * @param tKey transport key to be used for import stage. This should be according to keylabel schema defined in HSM
     * @param dKey device key as extracted from import file
     * @param deviceKeyLabel key label to be used for re-encryption of the device key. This should be according to keylabel schema configured.
     * @param keyType this should be configurable and known by the importer above (caller of this method)
     */
    @Override
    public HsmEncryptedKey importKey(TransportKey tKey, DeviceKey dKey, String deviceKeyLabel, KeyType keyType) throws EncryptBaseException{
        try {
            KeyImportResponse keyImportResponse = Energy.keyImport(tKey.toHsmFormat(dKey), tKey.getAsymmetricAlgorithm().getHsmSpecs().getPaddingAlgorithm(), dKey.toHsmFormat(), new KeyLabel(deviceKeyLabel), keyType.toProtectedSessionKeyCapability());
            ProtectedSessionKey psk = keyImportResponse.getProtectedSessionKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new HsmEncryptedKey(psk.getValue(), kekLabel);
        } catch (EncryptBaseException|FunctionFailedException e) {
            throw new EncryptBaseException(e);
        }
    }

    @Override
    public HsmEncryptedKey renewKey(byte[] deviceKey, String signKeyLabel, String deviceKeyLabel) throws EncryptBaseException {
        try {
            KeyLabel keyLabel = new KeyLabel(deviceKeyLabel);
            ProtectedSessionKey protectedSessionKey = new ProtectedSessionKey(new KeyLabel(signKeyLabel), deviceKey);
            KeyRenewalResponse response = Energy.cosemKeyRenewal(ProtectedSessionKeyCapability.SM_WK_CRYPTENC_RENEWAL,
                    protectedSessionKey,
                    keyLabel,
                    ProtectedSessionKeyType.AES_256);
            ProtectedSessionKey psk = response.getMdmStorageKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new HsmEncryptedKey(psk.getValue(), kekLabel);
        } catch (FunctionFailedException e) {
            throw new EncryptBaseException(e);
        } catch (Exception e) {
            throw new EncryptBaseException(e);
        }
    }
}
