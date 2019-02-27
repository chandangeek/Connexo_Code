package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * This class is just for test purpose for the time being
 */
@Component(name = "com.elster.jupiter.hsm.gogo.HsmEncryptGogo", service = {HsmEncryptGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=symmetricEncrypt", "osgi.command.function=symmetricDecrypt", "osgi.command.function=asymmetricEncrypt"}, immediate = true)
public class HsmEncryptGogo {

    private static final Logger logger = LoggerFactory.getLogger(HsmEncryptGogo.class);

    private volatile HsmEncryptionService hsmEncryptionService;

    public void symmetricEncrypt(String plainString, String label) throws HsmBaseException {
        logger.debug("symmetricEncrypt");
        byte[] encrypt = hsmEncryptionService.symmetricEncrypt(plainString.getBytes(), label);
        String b64Encrypt = Base64.getEncoder().encodeToString(encrypt);
        System.out.println("B64:" + b64Encrypt);

    }

    public void symmetricDecrypt(String b64Encrypt, String label) throws HsmBaseException {
        logger.debug("symmetricDecrypt");
        byte[] decrypt = hsmEncryptionService.symmetricDecrypt(Base64.getDecoder().decode(b64Encrypt), label);
        String b64Decrypt = Base64.getEncoder().encodeToString(decrypt);
        System.out.println("B64:" + b64Decrypt);
        System.out.println("String:" + new String(decrypt));

    }

    public void asymmetricEncrypt(String b64CipherText, String label) throws HsmBaseException {
        logger.debug("symmetricEncrypt");
        byte[] encrypt = hsmEncryptionService.asymmetricEncrypt(Base64.getDecoder().decode(b64CipherText.getBytes()), label, PaddingAlgorithm.EME_PKCS1_V1_5);
        String b64Encrypt = Base64.getEncoder().encodeToString(encrypt);
        System.out.println("B64:" + b64Encrypt);

    }

    @Reference
    public void setHsmEncryptionService(HsmEncryptionService hsmEncryptionService) {
        this.hsmEncryptionService = hsmEncryptionService;
    }
}
