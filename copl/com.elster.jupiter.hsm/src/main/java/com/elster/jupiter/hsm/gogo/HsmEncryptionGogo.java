package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.impl.HsmEncryptionServiceImpl;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;
import com.elster.jupiter.hsm.model.request.DecryptRequest;
import com.elster.jupiter.hsm.model.response.DecryptResponse;
import com.elster.jupiter.hsm.model.request.EncryptRequest;
import com.elster.jupiter.hsm.model.response.EncryptResponse;
import com.elster.jupiter.hsm.model.HsmBaseException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class is just for test purpose for the time being
 */
@Component(name = "com.elster.jupiter.hsm.gogo.HsmEncryptionGogo", service = {HsmEncryptionGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=jssEncrypt", "osgi.command.function=jssDecrypt"}, immediate = true)
public class HsmEncryptionGogo {

    private volatile HsmEncryptionService encService;

    public EncryptResponse jssEncrypt(String keyLabel, String stringToEncrypt) throws HsmBaseException {
        return this.encService.encrypt(new EncryptRequest(keyLabel, stringToEncrypt, SymmetricAlgorithm.AES_256_CBC));
    }

    public DecryptResponse jssDecrypt(String keyLabel, String encryptedString) throws HsmBaseException {
        return this.encService.decrypt(new DecryptRequest(keyLabel, encryptedString, SymmetricAlgorithm.AES_256_CBC));
    }

    @Reference
    public void setHsmEncryption(HsmEncryptionService encryptionService) {
        this.encService = encryptionService;
    }

}


