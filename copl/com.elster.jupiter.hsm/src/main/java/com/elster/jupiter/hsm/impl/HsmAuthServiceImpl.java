package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmAuthService;
import com.elster.jupiter.hsm.model.request.AuthDataDecryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataDecryptResponse;
import com.elster.jupiter.hsm.model.request.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.impl.HsmAuthServiceImpl", service = {HsmAuthServiceImpl.class},  immediate = true)
public class HsmAuthServiceImpl implements HsmAuthService {

    private HsmConfigurationServiceImpl hsmConfigurationService;

    @Override
    public AuthDataEncryptResponse encrypt(AuthDataEncryptRequest authDataEncRequest) throws HsmBaseException {
        this.hsmConfigurationService.checkInit();
        try {
            return new AuthDataEncryptResponse(Symmetric.authDataEncrypt(new KeyLabel(authDataEncRequest.getKeyLabel()), authDataEncRequest.getBytes(), authDataEncRequest.getAuthData(), authDataEncRequest.getInitialVector()));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public AuthDataDecryptResponse decrypt(AuthDataDecryptRequest authDataEncRequest) throws HsmBaseException {
        this.hsmConfigurationService.checkInit();
        try {
            return new AuthDataDecryptResponse(Symmetric.authDataDecrypt(new KeyLabel(authDataEncRequest.getKeyLabel()), authDataEncRequest.getBytes(), authDataEncRequest.getAuthData(), authDataEncRequest.getInitialVector(), authDataEncRequest.getAuthTag()));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }


    }

    @Reference
    public void setHsmConfigurationService(HsmConfigurationServiceImpl hsmConfigurationService){
        this.hsmConfigurationService = hsmConfigurationService;
    }
}
