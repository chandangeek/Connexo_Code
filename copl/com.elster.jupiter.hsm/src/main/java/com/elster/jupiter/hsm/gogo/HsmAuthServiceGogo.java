package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.impl.HsmAuthServiceImpl;
import com.elster.jupiter.hsm.model.request.AuthDataDecryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataDecryptResponse;
import com.elster.jupiter.hsm.model.request.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.response.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.HsmBaseException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.gogo.HsmAuthServiceGogo", service = {HsmAuthServiceGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=jssAuthEncrypt", "osgi.command.function=jssAuthDecrypt"}, immediate = true)
public class HsmAuthServiceGogo {

    private HsmAuthServiceImpl hsmAuthService;

    public AuthDataEncryptResponse jssAuthEncrypt(String keyLabel, String s) throws HsmBaseException {
        return this.hsmAuthService.encrypt(new AuthDataEncryptRequest(keyLabel, s.getBytes(), null, null));
    }

    public AuthDataDecryptResponse jssAuthDecrypt(String keyLabel, String s) throws HsmBaseException {
        return this.hsmAuthService.decrypt(new AuthDataDecryptRequest(keyLabel, s.getBytes(), null, null, null));
    }

    @Reference
    public void setHsmAuthService(HsmAuthServiceImpl hsmAuthService) {
        this.hsmAuthService = hsmAuthService;
    }


}
