package com.elster.jupiter.hsm.model.krypto;

import com.elster.jupiter.hsm.impl.HsmAlgorithmSpecs;
import com.elster.jupiter.hsm.model.EncryptBaseException;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.key.KEKEncryptionMethod;

public enum SymmetricAlgorithm implements Algorithm {
    AES_256_CBC("AES/CBC/PKCS5PADDING", "http://www.w3.org/2001/04/xmlenc#aes256-cbc") {
        @Override
        public HsmAlgorithmSpecs getHsmSpecs() {
            return new HsmAlgorithmSpecs(ChainingMode.CBC, PaddingAlgorithm.PKCS, KEKEncryptionMethod.CBC);
        }
    };

    private String cipherName;
    private String identifier;


    private SymmetricAlgorithm(String cipherName, String identifier) {
        this.cipherName = cipherName;
        this.identifier = identifier;
    }


    /**
     * Get cipher name
     *
     * @return
     */
    public String getCipher() {
        return cipherName;
    }

    public String getIdentifier() {
        return identifier;
    }


    /**
     * See https://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#aes256-cbc
     *
     * @param identifier
     * @return
     * @throws EncryptBaseException
     */
    public static SymmetricAlgorithm getByIdentifier(String identifier) throws EncryptBaseException {
        for (SymmetricAlgorithm symmetricAlgorithm : values()) {
            if (symmetricAlgorithm.getIdentifier().equals(identifier)) {
                return symmetricAlgorithm;
            }
        }
        throw new EncryptBaseException("Unknown symmetric algorithm:" + identifier);
    }

    @Override
    public Type getType() {
        return Type.SYMMETRIC;
    }



}
