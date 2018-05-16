package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.api.basecrypto.AuthDataEncryptionResponse;

public class AuthData {

    private final byte[] data;

    public AuthData(AuthDataEncryptionResponse authDataEncryptionResponse) {
        this.data = authDataEncryptionResponse.getData();
    }

    public byte[] getData() {
        return data;
    }
}
