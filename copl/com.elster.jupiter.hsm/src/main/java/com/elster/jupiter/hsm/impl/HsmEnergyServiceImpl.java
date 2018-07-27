package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmConfigurationService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmConfiguration;
import com.elster.jupiter.hsm.model.keys.HsmEncryptedKey;
import com.elster.jupiter.hsm.model.request.ImportKeyRequest;
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

    private static final int AES_KEY_LENGTH = 16;
    private static final int AES256_KEY_LENGTH = 32;

    static final String COMPONENTNAME = "HsmEnergyServiceImpl";


    private volatile HsmConfigurationService hsmConfigurationService;



    @Override
    public HsmEncryptedKey importKey(ImportKeyRequest importKeyRequest) throws HsmBaseException {
        try {
            HsmConfiguration hsmConfiguration = hsmConfigurationService.getHsmConfiguration();
            String encryptLabel = importKeyRequest.getImportLabel(hsmConfiguration);

            KeyImportResponse keyImportResponse = Energy.keyImport(importKeyRequest.getTransportKey(hsmConfiguration), importKeyRequest.getWrapperKeyAlgorithm().getHsmSpecs().getPaddingAlgorithm(), importKeyRequest.getDeviceKey(hsmConfiguration), new KeyLabel(encryptLabel), importKeyRequest.getImportSessionCapability(hsmConfiguration)
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
        } catch (Exception e) {
            throw new HsmBaseException(e);
        }
    }

    private ProtectedSessionKeyType getSessionKeyType(String renewLabel) throws HsmBaseException {

        Integer keyLength = hsmConfigurationService.getHsmConfiguration().get(renewLabel).getDeviceKeyLength();
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
