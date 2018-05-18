package com.elster.jupiter.hsm.model;

import com.elster.jupiter.hsm.EncryptionType;

public class EncryptRequest extends DecryptRequest {

    public EncryptRequest(String keyLabel, EncryptionType type, String clearString){
        super(keyLabel, type, clearString);
    }

    public EncryptRequest(String keyLabel, EncryptionType eType, String clearText, PaddingAlgorithm paddingAlgorithm, ChainingMode chainingMode) {
        super(keyLabel, eType, clearText, paddingAlgorithm, chainingMode);

    }
}
