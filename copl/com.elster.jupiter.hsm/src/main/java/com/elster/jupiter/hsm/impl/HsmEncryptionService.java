package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.console.HsmConfigurationService", service = {HsmEncryptionService.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=encrypt", "osgi.command.function=decrypt"}, immediate = true)
public class HsmEncryptionService {

    private HsmConfigurationService hsmConfigService;

    public EncryptionResponse encrypt(String label, String plainTextKey, String etype) {
        checkInit();
        EncryptionType type = EncryptionType.valueOf(etype.toUpperCase());
        try {
            KeyLabel keyLabel = new KeyLabel(label);
            byte[] encrypt = getEncrypt(plainTextKey, keyLabel, type);
            return new EncryptionResponse(encrypt);
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private byte[] getEncrypt(String plainTextKey, KeyLabel keyLabel, EncryptionType etype) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(etype)) {
            return Symmetric.encrypt(keyLabel, KeyDerivation.FIXED_KEY_ARRAY, plainTextKey.getBytes(), null, PaddingAlgorithm.ANSI_X9_23, ChainingMode.CBC).getData();
        }
        return Asymmetric.encrypt(keyLabel, plainTextKey.getBytes(), PaddingAlgorithm.ANSI_X9_23);
    }

    public DecryptResponse decrypt(String label, String cipherTxt, String etype) {
        try {
            byte[] decrypt = getDecrypt(label, cipherTxt, EncryptionType.valueOf(etype.toUpperCase()));
            return new DecryptResponse(decrypt);
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private byte[] getDecrypt(String label, String cipherTxt, EncryptionType etype) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(etype)) {
            return Symmetric.decrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, cipherTxt.getBytes(), null, PaddingAlgorithm.ANSI_X9_23, ChainingMode.CBC);
        }
      return   Asymmetric.decrypt(new KeyLabel(label), cipherTxt.getBytes(),  PaddingAlgorithm.ANSI_X9_23);
    }


    private void checkInit() {
        if (!hsmConfigService.isInit()) {
            throw new RuntimeException("JSS not initialized!");
        }
    }

    @Reference
    public void setHsmConfigService(HsmConfigurationService hsmConfigService){
        this.hsmConfigService = hsmConfigService;
    }

}
