package com.elster.jupiter.hsm.model.krypto;

import com.elster.jupiter.hsm.impl.HsmAlgorithmSpecs;

public interface Algorithm {
    /**
     *
     * @return cipher like string format (like AES/CBC/NoPadding)
     */
    String getCipher();

    Type getType();

    HsmAlgorithmSpecs getHsmSpecs();



}
