package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.EncryptionType;
import com.elster.jupiter.hsm.model.ChainingMode;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.PaddingAlgorithm;

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
