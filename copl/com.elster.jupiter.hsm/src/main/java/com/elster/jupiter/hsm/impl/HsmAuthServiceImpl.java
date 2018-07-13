package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmAuthService;
import com.elster.jupiter.hsm.HsmConfigurationService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.request.AuthDataDecryptRequest;
import com.elster.jupiter.hsm.model.request.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataDecryptResponse;
import com.elster.jupiter.hsm.model.response.AuthDataEncryptResponse;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.impl.HsmAuthServiceImpl", service = {HsmAuthService.class}, immediate = true)
public class HsmAuthServiceImpl implements HsmAuthService {

    private HsmConfigurationService hsmConfigurationService;

    @Override
    public AuthDataEncryptResponse encrypt(AuthDataEncryptRequest authDataEncRequest) throws HsmBaseException {
        if (hsmConfigurationService instanceof HsmConfigurationServiceImpl) {
            ((HsmConfigurationServiceImpl) this.hsmConfigurationService).checkInit();
        } else {
            throw new HsmBaseException("...");
        }
        try {
            return new AuthDataEncryptResponse(Symmetric.authDataEncrypt(new KeyLabel(authDataEncRequest.getKeyLabel()), authDataEncRequest.getBytes(), authDataEncRequest.getAuthData(), authDataEncRequest
                    .getInitialVector()));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }
    }

    @Override
    public AuthDataDecryptResponse decrypt(AuthDataDecryptRequest authDataEncRequest) throws HsmBaseException {
        if (hsmConfigurationService instanceof HsmConfigurationServiceImpl) {
            ((HsmConfigurationServiceImpl) this.hsmConfigurationService).checkInit();
        } else {
            throw new HsmBaseException("...");
        }
        try {
            return new AuthDataDecryptResponse(Symmetric.authDataDecrypt(new KeyLabel(authDataEncRequest.getKeyLabel()), authDataEncRequest.getBytes(), authDataEncRequest.getAuthData(), authDataEncRequest
                    .getInitialVector(), authDataEncRequest.getAuthTag()));
        } catch (FunctionFailedException e) {
            throw new HsmBaseException(e);
        }


    }

    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService) {
        this.hsmConfigurationService = hsmConfigurationService;
    }
}
