package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.impl.HsmAuthServiceImpl;
import com.elster.jupiter.hsm.model.EncryptedAuthData;
import com.elster.jupiter.hsm.model.HsmException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.gogo.HsmAuthServiceGogo", service = {HsmAuthServiceGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=jssAuthEncrypt"}, immediate = true)
public class HsmAuthServiceGogo {

    private HsmAuthServiceImpl hsmAuthService;

    public EncryptedAuthData jssAuthEncrypt(String keyLabel, String plainTxt) throws HsmException {
        return this.hsmAuthService.encrypt(keyLabel, plainTxt);
    }

    @Reference
    public void setHsmAuthService(HsmAuthServiceImpl hsmAuthService) {
        this.hsmAuthService = hsmAuthService;
    }


}
