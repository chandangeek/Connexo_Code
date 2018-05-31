package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.EncryptionType;
import com.elster.jupiter.hsm.model.krypto.ChainingMode;
import com.elster.jupiter.hsm.model.krypto.AsymmetricPaddingAlgorithm;

public class EncryptRequest extends DecryptRequest {

    public EncryptRequest(String keyLabel, EncryptionType eType, String clearText, AsymmetricPaddingAlgorithm paddingAlgorithm, ChainingMode chainingMode) {
        super(keyLabel, eType, clearText, paddingAlgorithm, chainingMode);

    }
}
