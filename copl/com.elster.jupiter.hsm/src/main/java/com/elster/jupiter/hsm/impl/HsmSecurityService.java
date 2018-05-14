package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.basecrypto.Asymmetric;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.basecrypto.Symmetric;
import com.atos.worldline.jss.api.basecrypto.SymmetricResponse;
import com.atos.worldline.jss.api.key.KeyLabel;
import com.atos.worldline.jss.api.key.derivation.KeyDerivation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.console.HsmConfigurationService", service = {HsmSecurityService.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=encrypt"}, immediate = true)
public class HsmSecurityService {

    private HsmConfigurationService hsmConfigService;

    public EncryptionResponse encrypt(String label, String plainTextKey, String etype) {
        checkInit();
        EncryptionType type = EncryptionType.valueOf(etype.toUpperCase());
        try {
            KeyLabel keyLabel = new KeyLabel(label);
            byte[] encrypt = delegate(plainTextKey, keyLabel, type);
            return new EncryptionResponse(encrypt);
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private byte[] delegate(String plainTextKey, KeyLabel keyLabel, EncryptionType type) throws FunctionFailedException {
        if (EncryptionType.SYMMETRIC.equals(type)) {
            return Symmetric.encrypt(keyLabel, KeyDerivation.FIXED_KEY_ARRAY, plainTextKey.getBytes(), null, PaddingAlgorithm.PKCS, ChainingMode.CBC).getData();
        }
        return Asymmetric.encrypt(keyLabel, plainTextKey.getBytes(), PaddingAlgorithm.PKCS);
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
