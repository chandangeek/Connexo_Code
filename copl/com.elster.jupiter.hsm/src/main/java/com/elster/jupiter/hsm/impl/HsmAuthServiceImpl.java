package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmAuthService;
import com.elster.jupiter.hsm.model.EncryptedAuthData;
import com.elster.jupiter.hsm.model.HsmException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.hsm.console.HsmAuthServiceImpl", service = {HsmAuthServiceImpl.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=authDataEncrypt"}, immediate = true)
public class HsmAuthServiceImpl implements HsmAuthService {


    @Override
    public EncryptedAuthData authDataEncrypt(String keyLabel, String plainTxt) throws HsmException{
        try {
            return new EncryptedAuthData(Symmetric.authDataEncrypt(new KeyLabel(keyLabel), plainTxt.getBytes(), null, null));
        } catch (FunctionFailedException e) {
            throw new HsmException(e);
        }
    }

}
