package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.FunctionFailedException;
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

    public EncryptionResponse encrypt(String label, String plainTextKey) {
        checkInit();
        try {
            KeyLabel keyLabel = new KeyLabel(label);
            SymmetricResponse encrypt = Symmetric.encrypt(keyLabel, KeyDerivation.FIXED_KEY_ARRAY, plainTextKey.getBytes(), null, PaddingAlgorithm.PKCS, ChainingMode.CBC);
            return new EncryptionResponse(encrypt);
        } catch (FunctionFailedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
