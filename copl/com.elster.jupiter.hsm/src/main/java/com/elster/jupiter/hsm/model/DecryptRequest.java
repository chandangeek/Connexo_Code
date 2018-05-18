package com.elster.jupiter.hsm.model;

import com.elster.jupiter.hsm.EncryptionType;

public class DecryptRequest extends Message {

    private final String keyLabel;
    private final EncryptionType type;
    private final PaddingAlgorithm paddingAlgorithm;
    private final ChainingMode chainingMode;

    public DecryptRequest(String keyLabel, EncryptionType type, String clearString, PaddingAlgorithm paddingAlgorithm, ChainingMode chainingMode) {
        super(clearString);
        this.keyLabel = keyLabel;
        this.type = type;
        this.paddingAlgorithm = paddingAlgorithm;
        this.chainingMode = chainingMode;
    }

    public DecryptRequest(String keyLabel, EncryptionType type, String clearString) {
        super(clearString);
        this.keyLabel = keyLabel;
        this.type = type;
        this.paddingAlgorithm = PaddingAlgorithm.ANSI_X9_23;
        this.chainingMode = ChainingMode.CBC;
    }


    public String getKeyLabel() {
        return keyLabel;
    }

    public EncryptionType getType() {
        return type;
    }

    public PaddingAlgorithm getPaddingAlgorithm() {
        return paddingAlgorithm;
    }

    public ChainingMode getChainingMode() {
        return chainingMode;
    }

}
