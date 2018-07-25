package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmConfigurationService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.keys.DeviceKey;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.hsm.model.keys.TransportKey;
import com.elster.jupiter.hsm.model.request.RenewKeyRequest;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.custom.energy.Energy;
import com.atos.worldline.jss.api.custom.energy.KeyImportResponse;
import com.atos.worldline.jss.api.custom.energy.KeyRenewalResponse;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKey;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyType;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.HsmEnergyServiceImpl", service = {HsmEnergyService.class}, immediate = true, property = "name=" + HsmEnergyServiceImpl.COMPONENTNAME)
public class HsmEnergyServiceImpl implements HsmEnergyService {

    public static final int AES_KEY_LENGTH = 16;
    private static final int AES256_KEY_LENGTH = 32;


    private volatile HsmConfigurationService hsmConfigurationService;

    /**
     *
     * @param tKey transport key to be used for import stage. This should be according to keylabel schema defined in HSM
     * @param dKey device key as extracted from import file
     * @param deviceKeyLabel key label to be used for re-encryption of the device key. This should be according to keylabel schema configured.
     * @param keyType this should be configurable and known by the importer above (caller of this method)
     */

    static final String COMPONENTNAME = "HsmEnergyServiceImpl";
    @Override
    public HsmEncryptedKey importKey(TransportKey tKey, DeviceKey dKey, String deviceKeyLabel, SessionKeyCapability sessionKeyCapability) throws HsmBaseException {
        try {
            KeyImportResponse keyImportResponse = Energy.keyImport(tKey.toHsmFormat(), tKey.getAsymmetricAlgorithm().getHsmSpecs().getPaddingAlgorithm(), dKey.toHsmFormat(), new KeyLabel(deviceKeyLabel), sessionKeyCapability
                    .toProtectedSessionKeyCapability());
            ProtectedSessionKey psk = keyImportResponse.getProtectedSessionKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new HsmEncryptedKey(psk.getValue(), kekLabel);
        } catch (HsmBaseException |FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    public HsmEncryptedKey renewKey(RenewKeyRequest renewKeyRequest) throws HsmBaseException {
        try {
            KeyLabel newLabel = new KeyLabel(renewKeyRequest.getRenewLabel());
            ProtectedSessionKey protectedSessionKey = new ProtectedSessionKey(new KeyLabel(renewKeyRequest.getActualLabel()),renewKeyRequest.getActualKey());
            KeyRenewalResponse response = Energy.cosemKeyRenewal(hsmConfigurationService.getHsmConfiguration().get(renewKeyRequest.getActualLabel()).getRenewSessionKeyCapability().toProtectedSessionKeyCapability(),
                    protectedSessionKey,
                    newLabel,
                    getSessionKeyType(renewKeyRequest.getRenewLabel()));
            ProtectedSessionKey psk = response.getMdmStorageKey();
            String kekLabel = ((KeyLabel) psk.getKek()).getValue();
            return new HsmEncryptedKey(psk.getValue(), kekLabel);
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        } catch (Exception e) {
            throw new HsmBaseException(e);
        }
    }

    private ProtectedSessionKeyType getSessionKeyType(String renewLabel) throws HsmBaseException {

        Integer keyLength = hsmConfigurationService.getHsmConfiguration().get(renewLabel).getKeyLength();
        if (keyLength == AES_KEY_LENGTH) {
            return ProtectedSessionKeyType.AES;
        }

        if (keyLength == AES256_KEY_LENGTH) {
            return ProtectedSessionKeyType.AES_256;
        }
        throw new HsmBaseException("Could not determine session key type for key length:" + keyLength +" configured on label:" + renewLabel);
    }


    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService) {
        this.hsmConfigurationService = hsmConfigurationService;
    }
}
