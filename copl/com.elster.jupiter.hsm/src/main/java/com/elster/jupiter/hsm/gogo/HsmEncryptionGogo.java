package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.EncryptionType;
import com.elster.jupiter.hsm.impl.HsmEncryptionServiceImpl;
import com.elster.jupiter.hsm.model.ChainingMode;
import com.elster.jupiter.hsm.model.PaddingAlgorithm;
import com.elster.jupiter.hsm.model.request.DecryptRequest;
import com.elster.jupiter.hsm.model.response.DecryptResponse;
import com.elster.jupiter.hsm.model.request.EncryptRequest;
import com.elster.jupiter.hsm.model.response.EncryptResponse;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.gogo.HsmEncryptionGogo", service = {HsmEncryptionGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=jssEncrypt", "osgi.command.function=jssDecrypt"}, immediate = true)
public class HsmEncryptionGogo {

    private HsmEncryptionServiceImpl encService;

    public EncryptResponse jssEncrypt(String keyLabel, String stringToEncrypt, String encryptionType) throws EncryptBaseException {
        return this.encService.encrypt(new EncryptRequest(keyLabel, EncryptionType.valueOf(encryptionType), stringToEncrypt, PaddingAlgorithm.ANSI_X9_23, ChainingMode.CBC));
    }

    public DecryptResponse jssDecrypt(String keyLabel, String encryptedString, String encryptionType) throws EncryptBaseException {
        return this.encService.decrypt(new DecryptRequest(keyLabel, EncryptionType.valueOf(encryptionType), encryptedString, PaddingAlgorithm.ANSI_X9_23, ChainingMode.CBC));
    }

    @Reference
    public void setHsmEncryption(HsmEncryptionServiceImpl encryptionService){
        this.encService = encryptionService;
    }

}


