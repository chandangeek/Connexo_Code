package com.elster.jupiter.hsm.model.request;

import com.elster.jupiter.hsm.model.krypto.Algorithm;

public class EncryptRequest extends DecryptRequest {

    public EncryptRequest(String keyLabel, String clearText, Algorithm algorithm) {
        super(keyLabel, clearText, algorithm);

    }
}
