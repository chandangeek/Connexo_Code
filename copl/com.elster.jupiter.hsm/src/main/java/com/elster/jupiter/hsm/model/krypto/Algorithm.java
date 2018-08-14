package com.elster.jupiter.hsm.model.krypto;

public interface Algorithm {
    /**
     *
     * @return cipher like string format (like AES/CBC/NoPadding)
     */
    String getCipher();

    Type getType();

    HsmAlgorithmSpecs getHsmSpecs();



}
