package com.elster.jupiter.hsm.model.krypto;


import com.elster.jupiter.hsm.impl.HsmAlgorithmSpecs;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.key.KEKEncryptionMethod;

public enum AsymmetricAlgorithm implements Algorithm {
    RSA_15("RSA/ECB/PKCS1Padding") {
        @Override
        public HsmAlgorithmSpecs getHsmSpecs() {
            return new HsmAlgorithmSpecs(ChainingMode.ECB, PaddingAlgorithm.EME_PKCS1_V1_5, KEKEncryptionMethod.CBC);
        }
    };

    private String cipherName;

    AsymmetricAlgorithm(String cipherName) {
        this.cipherName = cipherName;
    }

    /**
     * Get cipher name
     *
     * @return
     */
    public String getCipher() {
        return this.cipherName;
    }


    @Override
    public Type getType() {
        return Type.ASYMMETRIC;
    }
}
