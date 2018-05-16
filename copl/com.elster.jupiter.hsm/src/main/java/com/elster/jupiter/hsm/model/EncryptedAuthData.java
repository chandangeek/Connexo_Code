package com.elster.jupiter.hsm.model;

import com.atos.worldline.jss.api.basecrypto.AuthDataEncryptionResponse;

public class EncryptedAuthData {

    private final byte[] data;

    public EncryptedAuthData(AuthDataEncryptionResponse authDataEncryptionResponse) {
        this.data = authDataEncryptionResponse.getData();
    }

    public byte[] getData() {
        return data;
    }
}
