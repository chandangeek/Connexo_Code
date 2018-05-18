package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.EncryptionType;
import com.elster.jupiter.hsm.impl.HsmEncryptionServiceImpl;
import com.elster.jupiter.hsm.model.ChainingMode;
import com.elster.jupiter.hsm.model.DecryptRequest;
import com.elster.jupiter.hsm.model.DecryptResponse;
import com.elster.jupiter.hsm.model.EncryptRequest;
import com.elster.jupiter.hsm.model.EncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.PaddingAlgorithm;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.gogo.HsmEncryptionGogo", service = {HsmEncryptionGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=jssEncrypt", "osgi.command.function=jssDecrypt"}, immediate = true)
public class HsmEncryptionGogo {

    private HsmEncryptionServiceImpl encService;

    public EncryptResponse jssEncrypt(String keyLabel, String stringToEncrypt, String encryptionType) throws EncryptBaseException {
        return this.encService.encrypt(new EncryptRequest(keyLabel, EncryptionType.valueOf(encryptionType), stringToEncrypt));
    }

    public DecryptResponse jssDecrypt(String keyLabel, String encryptedString, String encryptionType) throws EncryptBaseException {
        return this.encService.decrypt(new DecryptRequest(keyLabel, EncryptionType.valueOf(encryptionType), encryptedString));
    }

    @Reference
    public void setHsmEncryption(HsmEncryptionServiceImpl encryptionService){
        this.encService = encryptionService;
    }

}


