package com.elster.jupiter.hsm.model.krypto;

import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.api.key.KEKEncryptionMethod;

public enum SymmetricAlgorithm implements Algorithm {
    AES_256_CBC("AES/CBC/PKCS5PADDING", "http://www.w3.org/2001/04/xmlenc#aes256-cbc", 32) {
        @Override
        public HsmAlgorithmSpecs getHsmSpecs() {
            return new HsmAlgorithmSpecs(ChainingMode.CBC, PaddingAlgorithm.EME_PKCS1_V1_5, KEKEncryptionMethod.CBCPAD_PKCS7);
        }
    };

    private String cipherName;
    private String identifier;
    private int keySize;

    /**
     *
     * @param cipherName
     * @param identifier
     * @param keySize expressed in bytes, therefore AES 256 is 32 bytes
     */
    SymmetricAlgorithm(String cipherName, String identifier, int keySize) {
        this.cipherName = cipherName;
        this.identifier = identifier;
        this.keySize = keySize;
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

    public int getKeySize(){  return this.keySize;  }

    public String getAlgorithm(){
        return this.cipherName.split("/")[0];
    }

    public String getChainingMode(){
        return this.cipherName.split("/")[1];
    }

    public String getPadding(){
        return this.cipherName.split("/")[2];
    }



    /**
     * See https://www.w3.org/TR/2002/REC-xmlenc-core-20021210/Overview.html#aes256-cbc
     *
     * @param identifier
     * @return
     * @throws HsmBaseException
     */
    public static SymmetricAlgorithm getByIdentifier(String identifier) throws HsmBaseException {
        for (SymmetricAlgorithm symmetricAlgorithm : values()) {
            if (symmetricAlgorithm.getIdentifier().equals(identifier)) {
                return symmetricAlgorithm;
            }
        }
        throw new HsmBaseException("Unknown symmetric algorithm:" + identifier);
    }

    @Override
    public Type getType() {
        return Type.SYMMETRIC;
    }



}
