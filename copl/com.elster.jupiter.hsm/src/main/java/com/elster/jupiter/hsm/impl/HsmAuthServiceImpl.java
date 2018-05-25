package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmAuthService;
import com.elster.jupiter.hsm.model.request.AuthDataDecryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataDecryptResponse;
import com.elster.jupiter.hsm.model.request.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.impl.HsmAuthServiceImpl", service = {HsmAuthServiceImpl.class},  immediate = true)
public class HsmAuthServiceImpl implements HsmAuthService {

    private HsmConfigurationService hsmConfigurationService;

    @Override
    public AuthDataEncryptResponse encrypt(AuthDataEncryptRequest authDataEncRequest) throws EncryptBaseException {
        this.hsmConfigurationService.checkInit();
        try {
            return new AuthDataEncryptResponse(Symmetric.authDataEncrypt(new KeyLabel(authDataEncRequest.getKeyLabel()), authDataEncRequest.getBytes(), authDataEncRequest.getAuthData(), authDataEncRequest.getInitialVector()));
        } catch (FunctionFailedException e) {
            throw new EncryptBaseException(e);
        }
    }

    @Override
    public AuthDataDecryptResponse decrypt(AuthDataDecryptRequest authDataEncRequest) throws EncryptBaseException {
        this.hsmConfigurationService.checkInit();
        try {
            return new AuthDataDecryptResponse(Symmetric.authDataDecrypt(new KeyLabel(authDataEncRequest.getKeyLabel()), authDataEncRequest.getBytes(), authDataEncRequest.getAuthData(), authDataEncRequest.getInitialVector(), authDataEncRequest.getAuthTag()));
        } catch (FunctionFailedException e) {
            throw new EncryptBaseException(e);
        }


    }

    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService){
        this.hsmConfigurationService = hsmConfigurationService;
    }
}
