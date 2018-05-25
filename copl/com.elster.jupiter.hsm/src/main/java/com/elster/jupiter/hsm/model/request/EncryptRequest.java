package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.EncryptionType;
import com.elster.jupiter.hsm.model.ChainingMode;
import com.elster.jupiter.hsm.model.PaddingAlgorithm;
import com.elster.jupiter.hsm.model.request.DecryptRequest;

public class EncryptRequest extends DecryptRequest {

    public EncryptRequest(String keyLabel, EncryptionType eType, String clearText, PaddingAlgorithm paddingAlgorithm, ChainingMode chainingMode) {
        super(keyLabel, eType, clearText, paddingAlgorithm, chainingMode);

    }
}
