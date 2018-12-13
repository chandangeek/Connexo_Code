package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.krypto.Algorithm;

public class DecryptRequest extends Message {

    private final String keyLabel;
    private final Algorithm algorithm;

    public DecryptRequest(String keyLabel, String clearString, Algorithm algorithm) {
        super(clearString);
        this.keyLabel = keyLabel;
        this.algorithm = algorithm;
    }


    public String getKeyLabel() {
        return keyLabel;
    }


    public Algorithm getAlgorithm() { return algorithm; }
}
