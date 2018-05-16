package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.hsm.console.HsmAuthService", service = {HsmAuthService.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=authDataEncrypt"}, immediate = true)
public class HsmAuthService {


    public AuthData authDataEncrypt(String keyLabel, String plainTxt){
        try {
            return new AuthData(Symmetric.authDataEncrypt(new KeyLabel(keyLabel), plainTxt.getBytes(), null, null));
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
