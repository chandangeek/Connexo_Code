package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.impl.HsmAuthServiceImpl;
import com.elster.jupiter.hsm.model.AuthDataEncryptRequest;
import com.elster.jupiter.hsm.model.AuthDataEncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.gogo.HsmAuthServiceGogo", service = {HsmAuthServiceGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=jssAuthEncrypt"}, immediate = true)
public class HsmAuthServiceGogo {

    private HsmAuthServiceImpl hsmAuthService;

    public AuthDataEncryptResponse jssAuthEncrypt(String keyLabel, String s) throws EncryptBaseException {
        return this.hsmAuthService.encrypt(new AuthDataEncryptRequest(keyLabel, s.getBytes(), null, null));
    }

    @Reference
    public void setHsmAuthService(HsmAuthServiceImpl hsmAuthService) {
        this.hsmAuthService = hsmAuthService;
    }


}
