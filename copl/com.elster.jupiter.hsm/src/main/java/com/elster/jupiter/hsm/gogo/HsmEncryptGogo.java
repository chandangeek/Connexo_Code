package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * This class is just for test purpose for the time being
 */
@Component(name = "com.elster.jupiter.hsm.gogo.HsmEncryptGogo", service = {HsmEncryptGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=symmetricEncrypt", "osgi.command.function=symmetricDecrypt", "osgi.command.function=asymmetricEncrypt", "osgi.command.function=asymmetricDecrypt"}, immediate = true)
public class HsmEncryptGogo {

    private static final Logger logger = LoggerFactory.getLogger(HsmEncryptGogo.class);
    public static final PaddingAlgorithm ASYMM_PADDING_ALGORITHM = PaddingAlgorithm.EME_PKCS1_V1_5;

    private volatile HsmEncryptionService hsmEncryptionService;

    public void symmetricEncrypt(String plainString, String label) throws HsmBaseException {
        logger.debug("symmetricEncrypt");
        byte[] encrypt = hsmEncryptionService.symmetricEncrypt(plainString.getBytes(), label, null, ChainingMode.CBC, PaddingAlgorithm.PKCS);
        String b64Encrypt = Base64.getEncoder().encodeToString(encrypt);
        System.out.println("B64:" + b64Encrypt);

    }

    public void symmetricDecrypt(String b64CipherText, String label) throws HsmBaseException {
        logger.debug("symmetricDecrypt");
        byte[] decrypted = hsmEncryptionService.symmetricDecrypt(Base64.getDecoder().decode(b64CipherText), label, null, ChainingMode.CBC, PaddingAlgorithm.PKCS);
        String b64Decrypt = Base64.getEncoder().encodeToString(decrypted);
        System.out.println("B64:" + b64Decrypt);
        System.out.println("String:" + new String(decrypted));

    }

    public void asymmetricEncrypt(String text, String label) throws HsmBaseException {
        logger.debug("symmetricEncrypt");
        byte[] encrypt = hsmEncryptionService.asymmetricEncrypt(text.getBytes(), label, ASYMM_PADDING_ALGORITHM);
        String b64Encrypt = Base64.getEncoder().encodeToString(encrypt);
        System.out.println("B64:" + b64Encrypt);
    }

    public void asymmetricDecrypt(String b64CipherText, String label) throws HsmBaseException {
        logger.debug("symmetricEncrypt");
        byte[] decrypted = hsmEncryptionService.asymmetricDecrypt(Base64.getDecoder().decode(b64CipherText.getBytes()), label, ASYMM_PADDING_ALGORITHM);
        String b64Encrypt = Base64.getEncoder().encodeToString(decrypted);
        System.out.println("B64:" + b64Encrypt);
        System.out.println("String:" + new String(decrypted));
    }


    @Reference
    public void setHsmEncryptionService(HsmEncryptionService hsmEncryptionService) {
        this.hsmEncryptionService = hsmEncryptionService;
    }
}
