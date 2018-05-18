package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.EncryptionType;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.model.DecryptResponse;
import com.elster.jupiter.hsm.model.EncryptionResponse;
import com.elster.jupiter.hsm.model.HsmException;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.console.HsmEncryptionServiceImpl", service = {HsmEncryptionServiceImpl.class}, immediate = true)
public class HsmEncryptionServiceImpl implements HsmEncryptionService {

    private HsmConfigurationService hsmConfigService;

    @Override
    public EncryptionResponse encrypt(String label, String plainTextKey, String etype) throws HsmException {
        this.hsmConfigService.checkInit();
        EncryptionType type = EncryptionType.valueOf(etype.toUpperCase());
        try {
            KeyLabel keyLabel = new KeyLabel(label);
            byte[] encrypt = getEncrypt(plainTextKey, keyLabel, type);
            return new EncryptionResponse(encrypt);
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new HsmException(e);
        }
    }

    private byte[] getEncrypt(String plainTextKey, KeyLabel keyLabel, EncryptionType etype) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(etype)) {
            return Symmetric.encrypt(keyLabel, KeyDerivation.FIXED_KEY_ARRAY, plainTextKey.getBytes(), null, PaddingAlgorithm.ANSI_X9_23, ChainingMode.CBC).getData();
        }
        return Asymmetric.encrypt(keyLabel, plainTextKey.getBytes(), PaddingAlgorithm.ANSI_X9_23);
    }

    @Override
    public DecryptResponse decrypt(String label, String cipherTxt, String etype) throws HsmException {
        this.hsmConfigService.checkInit();
        try {
            byte[] decrypt = getDecrypt(label, cipherTxt, EncryptionType.valueOf(etype.toUpperCase()));
            return new DecryptResponse(decrypt);
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new HsmException(e);
        }

    }

    private byte[] getDecrypt(String label, String cipherTxt, EncryptionType etype) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(etype)) {
            return Symmetric.decrypt(new KeyLabel(label), KeyDerivation.FIXED_KEY_ARRAY, cipherTxt.getBytes(), null, PaddingAlgorithm.ANSI_X9_23, ChainingMode.CBC);
        }
      return   Asymmetric.decrypt(new KeyLabel(label), cipherTxt.getBytes(),  PaddingAlgorithm.ANSI_X9_23);
    }


    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigService){
        this.hsmConfigService = hsmConfigService;
    }

}
