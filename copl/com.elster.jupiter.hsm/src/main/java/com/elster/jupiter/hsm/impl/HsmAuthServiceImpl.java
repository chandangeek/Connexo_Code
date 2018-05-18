package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmAuthService;
import com.elster.jupiter.hsm.model.EncryptedAuthData;
import com.elster.jupiter.hsm.model.HsmException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.hsm.impl.HsmAuthServiceImpl", service = {HsmAuthServiceImpl.class},  immediate = true)
public class HsmAuthServiceImpl implements HsmAuthService {

    private HsmConfigurationService hsmCfgService;

    @Override
    public EncryptedAuthData encrypt(String keyLabel, String plainTxt) throws HsmException{
        try {
            checkInit();
            return new EncryptedAuthData(Symmetric.authDataEncrypt(new KeyLabel(keyLabel), plainTxt.getBytes(), null, null));
        } catch (FunctionFailedException e) {
            throw new HsmException(e);
        }
    }

    private void checkInit() {
        if (!hsmCfgService.isInit()) {
            throw new RuntimeException("JSS not initialized!");
        }
    }

    @Reference
    public void setHsmCfgService (HsmConfigurationService hsmCfgService) {
        this.hsmCfgService = hsmCfgService;
    }

}
