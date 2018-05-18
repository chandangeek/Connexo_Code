package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.impl.HsmEncryptionServiceImpl;
import com.elster.jupiter.hsm.model.DecryptResponse;
import com.elster.jupiter.hsm.model.EncryptionResponse;
import com.elster.jupiter.hsm.model.HsmException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.gogo.HsmEncryptionGogo", service = {HsmEncryptionGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=jssEncrypt", "osgi.command.function=jssDecrypt"}, immediate = true)
public class HsmEncryptionGogo {

    private HsmEncryptionServiceImpl encService;

    public EncryptionResponse jssEncrypt(String label, String plainTextKey, String etype) throws HsmException {
        return this.encService.encrypt(label, plainTextKey, etype);
    }

    public DecryptResponse jssDecrypt(String label, String cipherTxt, String etype) throws HsmException {
        return this.encService.decrypt(label, cipherTxt, etype);
    }

    @Reference
    public void setHsmEncryption(HsmEncryptionServiceImpl encryptionService){
        this.encService = encryptionService;
    }

}


